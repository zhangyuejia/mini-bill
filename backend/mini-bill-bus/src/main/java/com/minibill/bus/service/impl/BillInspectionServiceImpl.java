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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minibill.common.constant.Constants.*;

/**
 * 账单巡检服务实现
 * 校验当月账单的水电表底读数与上月账单是否一致
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillInspectionServiceImpl implements BillInspectionService {

    private final BusBillMapper billMapper;
    private final BusFamilyMemberMapper familyMemberMapper;
    private final BusMessageMapper messageMapper;
    private final MessageService messageService;

    @Override
    public void inspectBills() {
        // 1. 计算当月和上月账期
        int currentPeriod = computeCurrentPeriod();
        int prevPeriod = computePreviousPeriod(currentPeriod);

        log.info("账单巡检开始：当月账期={}, 上月账期={}", currentPeriod, prevPeriod);

        // 2. 查询当月所有账单
        List<BusBill> currentBills = billMapper.selectList(
                new LambdaQueryWrapper<BusBill>()
                        .eq(BusBill::getDelFlag, DEL_FLAG_NORMAL)
                        .eq(BusBill::getPeriod, currentPeriod));

        if (currentBills.isEmpty()) {
            log.info("当月无账单记录，跳过巡检");
            return;
        }

        // 3. 查询上月所有账单
        List<BusBill> prevBills = billMapper.selectList(
                new LambdaQueryWrapper<BusBill>()
                        .eq(BusBill::getDelFlag, DEL_FLAG_NORMAL)
                        .eq(BusBill::getPeriod, prevPeriod));

        // 4. 构建上月账单查找Map（key = familyId_addressId）
        Map<String, BusBill> prevBillMap = new HashMap<>();
        for (BusBill prevBill : prevBills) {
            String key = prevBill.getFamilyId() + "_" + prevBill.getAddressId();
            prevBillMap.put(key, prevBill);
        }

        // 5. 逐条比对
        List<Discrepancy> discrepancies = new ArrayList<>();
        for (BusBill currentBill : currentBills) {
            String key = currentBill.getFamilyId() + "_" + currentBill.getAddressId();
            BusBill prevBill = prevBillMap.get(key);
            if (prevBill == null) {
                continue;
            }

            boolean waterMismatch = false;
            boolean electricMismatch = false;

            // 水费比对：当月上月表底 vs 上月当月表底
            if (bothHaveValue(currentBill.getWaterPrevReading(), prevBill.getWaterCurrReading())) {
                if (currentBill.getWaterPrevReading().compareTo(prevBill.getWaterCurrReading()) != 0) {
                    waterMismatch = true;
                }
            }

            // 电费比对：当月上月表底 vs 上月当月表底
            if (bothHaveValue(currentBill.getElectricPrevReading(), prevBill.getElectricCurrReading())) {
                if (currentBill.getElectricPrevReading().compareTo(prevBill.getElectricCurrReading()) != 0) {
                    electricMismatch = true;
                }
            }

            if (waterMismatch || electricMismatch) {
                Discrepancy disc = new Discrepancy();
                disc.familyId = currentBill.getFamilyId();
                disc.billId = currentBill.getId();
                disc.currentPeriod = currentPeriod;
                disc.prevPeriod = prevPeriod;
                disc.waterMismatch = waterMismatch;
                disc.electricMismatch = electricMismatch;
                disc.waterPrevReading = currentBill.getWaterPrevReading();
                disc.prevWaterCurrReading = prevBill.getWaterCurrReading();
                disc.electricPrevReading = currentBill.getElectricPrevReading();
                disc.prevElectricCurrReading = prevBill.getElectricCurrReading();
                discrepancies.add(disc);
            }
        }

        if (discrepancies.isEmpty()) {
            log.info("账单巡检完成：未发现不一致");
            return;
        }

        log.info("账单巡检发现{}处不一致", discrepancies.size());

        // 6. 为每个不一致项通知所有家庭成员
        for (Discrepancy disc : discrepancies) {
            List<BusFamilyMember> members = familyMemberMapper.selectList(
                    new LambdaQueryWrapper<BusFamilyMember>()
                            .eq(BusFamilyMember::getFamilyId, disc.familyId)
                            .eq(BusFamilyMember::getStatus, 0));

            for (BusFamilyMember member : members) {
                // 去重：同一天、同一类型、同一用户、同一账单的消息不重复创建
                boolean exists = messageMapper.selectCount(new LambdaQueryWrapper<BusMessage>()
                        .eq(BusMessage::getType, MSG_TYPE_BILL_INSPECTION)
                        .eq(BusMessage::getUserId, member.getUserId())
                        .eq(BusMessage::getRelatedBillId, disc.billId)
                        .ge(BusMessage::getCreateTime, LocalDate.now().atStartOfDay())
                ) > 0;

                if (exists) {
                    continue;
                }

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
            }
        }

        log.info("账单巡检完成");
    }

    /**
     * 计算当前账期（如202607）
     */
    private int computeCurrentPeriod() {
        LocalDate now = LocalDate.now();
        return now.getYear() * 100 + now.getMonthValue();
    }

    /**
     * 计算上一账期（跨年处理）
     */
    private int computePreviousPeriod(int period) {
        int year = period / 100;
        int month = period % 100;
        if (month == 1) {
            return (year - 1) * 100 + 12;
        }
        return period - 1;
    }

    /**
     * 两个值均非null且不为0
     */
    private boolean bothHaveValue(BigDecimal a, BigDecimal b) {
        return a != null && b != null
                && a.compareTo(BigDecimal.ZERO) != 0
                && b.compareTo(BigDecimal.ZERO) != 0;
    }

    /**
     * 构建不一致消息内容
     */
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

    /**
     * 不一致记录内部类
     */
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
