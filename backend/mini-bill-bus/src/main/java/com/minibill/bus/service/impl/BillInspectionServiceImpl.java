package com.minibill.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minibill.bus.entity.BusBill;
import com.minibill.bus.entity.BusFamilyMember;
import com.minibill.bus.entity.BusMessage;
import com.minibill.bus.mapper.BusBillMapper;
import com.minibill.bus.mapper.BusFamilyMemberMapper;
import com.minibill.bus.mapper.BusMessageMapper;
import com.minibill.bus.service.BillInspectionService;
import com.minibill.bus.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.minibill.common.constant.Constants.*;

/**
 * 账单巡检服务实现
 * 校验近N个月账单的水电表底读数在相邻月份间是否一致
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillInspectionServiceImpl implements BillInspectionService {

    private final BusBillMapper billMapper;
    private final BusFamilyMemberMapper familyMemberMapper;
    private final BusMessageMapper messageMapper;
    private final MessageService messageService;

    /** 巡检回溯月数，默认36（近3年） */
    @Value("${minibill.inspection.lookback-months:36}")
    private int lookbackMonths;

    @Override
    public void inspectBills() {
        int currentPeriod = computeCurrentPeriod();
        int startPeriod = subtractMonths(currentPeriod, lookbackMonths - 1);

        log.info("账单巡检开始：时间范围 {} ~ {}", startPeriod, currentPeriod);

        // 1. 批量查出整个时间窗口内所有账单（仅一次DB查询）
        List<BusBill> allBills = billMapper.selectList(new LambdaQueryWrapper<BusBill>()
                .ge(BusBill::getPeriod, startPeriod)
                .le(BusBill::getPeriod, currentPeriod)
                .orderByAsc(BusBill::getPeriod));

        if (allBills.isEmpty()) {
            log.info("巡检范围内无账单记录，跳过巡检");
            return;
        }

        // 2. 按 (familyId, addressId) 分组，组内按 period 排序
        Map<String, List<BusBill>> grouped = allBills.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getFamilyId() + "_" + b.getAddressId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        // 3. 逐组比对相邻月份的账单
        List<Discrepancy> discrepancies = new ArrayList<>();
        for (List<BusBill> bills : grouped.values()) {
            if (bills.size() < 2) continue;

            for (int i = 0; i < bills.size() - 1; i++) {
                BusBill prev = bills.get(i);
                BusBill curr = bills.get(i + 1);

                // 只比对相邻月份（period 差 1 或跨年 89）
                int diff = periodDiff(prev.getPeriod(), curr.getPeriod());
                if (diff != 1) continue;

                boolean waterMismatch = false;
                boolean electricMismatch = false;

                if (bothHaveValue(curr.getWaterPrevReading(), prev.getWaterCurrReading())) {
                    if (curr.getWaterPrevReading().compareTo(prev.getWaterCurrReading()) != 0) {
                        waterMismatch = true;
                    }
                }
                if (bothHaveValue(curr.getElectricPrevReading(), prev.getElectricCurrReading())) {
                    if (curr.getElectricPrevReading().compareTo(prev.getElectricCurrReading()) != 0) {
                        electricMismatch = true;
                    }
                }

                if (waterMismatch || electricMismatch) {
                    Discrepancy disc = new Discrepancy();
                    disc.familyId = curr.getFamilyId();
                    disc.billId = curr.getId();
                    disc.currentPeriod = curr.getPeriod();
                    disc.prevPeriod = prev.getPeriod();
                    disc.waterMismatch = waterMismatch;
                    disc.electricMismatch = electricMismatch;
                    disc.waterPrevReading = curr.getWaterPrevReading();
                    disc.prevWaterCurrReading = prev.getWaterCurrReading();
                    disc.electricPrevReading = curr.getElectricPrevReading();
                    disc.prevElectricCurrReading = prev.getElectricCurrReading();
                    discrepancies.add(disc);
                }
            }
        }

        if (discrepancies.isEmpty()) {
            log.info("账单巡检完成：未发现不一致");
            return;
        }

        log.info("账单巡检发现{}处不一致", discrepancies.size());

        // 4. 收集所有涉及的 familyId，批量查成员
        Set<Long> familyIds = discrepancies.stream()
                .map(d -> d.familyId)
                .collect(Collectors.toSet());
        List<BusFamilyMember> allMembers = familyMemberMapper.selectList(
                new LambdaQueryWrapper<BusFamilyMember>()
                        .in(BusFamilyMember::getFamilyId, familyIds)
                        .eq(BusFamilyMember::getStatus, 0));
        Map<Long, List<BusFamilyMember>> memberMap = allMembers.stream()
                .collect(Collectors.groupingBy(BusFamilyMember::getFamilyId));

        // 5. 批量查询今日已有的同类型消息（去重用）
        List<BusMessage> todayMessages = messageMapper.selectList(new LambdaQueryWrapper<BusMessage>()
                .eq(BusMessage::getType, MSG_TYPE_BILL_INSPECTION)
                .ge(BusMessage::getCreateTime, LocalDate.now().atStartOfDay()));
        Set<String> existingKeys = todayMessages.stream()
                .map(m -> m.getUserId() + "_" + m.getRelatedBillId())
                .collect(Collectors.toSet());

        // 6. 批量生成消息并入库
        int created = 0;
        for (Discrepancy disc : discrepancies) {
            List<BusFamilyMember> members = memberMap.get(disc.familyId);
            if (members == null || members.isEmpty()) continue;

            for (BusFamilyMember member : members) {
                String key = member.getUserId() + "_" + disc.billId;
                if (existingKeys.contains(key)) continue;

                BusMessage message = new BusMessage();
                message.setTitle("账单数据不一致提醒");
                message.setContent(buildMessageContent(disc));
                message.setType(MSG_TYPE_BILL_INSPECTION);
                message.setIsRead(0);
                message.setUserId(member.getUserId());
                message.setFamilyId(disc.familyId);
                message.setRelatedBillId(disc.billId);
                message.setCreateTime(LocalDateTime.now());

                messageService.createMessage(message);
                existingKeys.add(key); // 内存去重，同批次不重复
                created++;
            }
        }

        log.info("账单巡检完成：共生成{}条新消息", created);
    }

    private int computeCurrentPeriod() {
        LocalDate now = LocalDate.now();
        return now.getYear() * 100 + now.getMonthValue();
    }

    /** period 减去 N 个月（跨年处理） */
    private int subtractMonths(int period, int n) {
        int year = period / 100;
        int month = period % 100;
        int totalMonths = year * 12 + (month - 1) - n;
        return (totalMonths / 12) * 100 + (totalMonths % 12) + 1;
    }

    /** 两个 period 之间相差的月数 */
    private int periodDiff(int from, int to) {
        int fy = from / 100, fm = from % 100;
        int ty = to / 100, tm = to % 100;
        return (ty * 12 + tm) - (fy * 12 + fm);
    }

    private boolean bothHaveValue(BigDecimal a, BigDecimal b) {
        return a != null && b != null
                && a.compareTo(BigDecimal.ZERO) != 0
                && b.compareTo(BigDecimal.ZERO) != 0;
    }

    private String buildMessageContent(Discrepancy disc) {
        StringBuilder sb = new StringBuilder();
        sb.append("账期").append(disc.currentPeriod).append("与").append(disc.prevPeriod).append("数据不一致：");
        if (disc.waterMismatch) {
            sb.append("水费上月表底为").append(disc.waterPrevReading)
                    .append("，但").append(disc.prevPeriod)
                    .append("账期当月表底为").append(disc.prevWaterCurrReading)
                    .append("，两者不一致；");
        }
        if (disc.electricMismatch) {
            sb.append("电费上月表底为").append(disc.electricPrevReading)
                    .append("，但").append(disc.prevPeriod)
                    .append("账期当月表底为").append(disc.prevElectricCurrReading)
                    .append("，两者不一致；");
        }
        sb.append("请核对账单数据。");
        return sb.toString();
    }

    private static class Discrepancy {
        Long familyId;
        Long billId;
        int currentPeriod;
        int prevPeriod;
        boolean waterMismatch;
        boolean electricMismatch;
        BigDecimal waterPrevReading;
        BigDecimal prevWaterCurrReading;
        BigDecimal electricPrevReading;
        BigDecimal prevElectricCurrReading;
    }
}
