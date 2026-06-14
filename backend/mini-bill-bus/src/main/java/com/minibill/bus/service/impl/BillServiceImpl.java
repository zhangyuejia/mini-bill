package com.minibill.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.exception.BusinessException;
import com.minibill.bus.entity.BusAttachment;
import com.minibill.bus.entity.BusBill;
import com.minibill.bus.mapper.BusAttachmentMapper;
import com.minibill.bus.mapper.BusBillMapper;
import com.minibill.bus.service.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.minibill.common.constant.Constants.*;

/**
 * 账单管理服务实现
 */
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BusBillMapper billMapper;
    private final BusAttachmentMapper attachmentMapper;

    @Override
    public Page<BusBill> pageBill(Integer pageNum, Integer pageSize, Long familyId, Long addressId, Integer periodStart, Integer periodEnd) {
        Page<BusBill> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BusBill> wrapper = new LambdaQueryWrapper<BusBill>()
                .eq(BusBill::getFamilyId, familyId)
                .eq(addressId != null, BusBill::getAddressId, addressId)
                .ge(periodStart != null, BusBill::getPeriod, periodStart)
                .le(periodEnd != null, BusBill::getPeriod, periodEnd)
                .eq(BusBill::getDelFlag, DEL_FLAG_NORMAL)
                .orderByDesc(BusBill::getPeriod);
        Page<BusBill> result = billMapper.selectPage(page, wrapper);

        // 批量查询附件
        List<BusBill> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Long> billIds = records.stream().map(BusBill::getId).collect(Collectors.toList());
            List<BusAttachment> allAttachments = attachmentMapper.selectList(
                    new LambdaQueryWrapper<BusAttachment>()
                            .eq(BusAttachment::getBizType, BIZ_TYPE_BILL)
                            .in(BusAttachment::getBizId, billIds));
            Map<Long, List<BusAttachment>> attachmentMap = allAttachments.stream()
                    .collect(Collectors.groupingBy(BusAttachment::getBizId));
            records.forEach(bill -> bill.setAttachments(attachmentMap.getOrDefault(bill.getId(), new ArrayList<>())));
        }

        return result;
    }

    @Override
    public BusBill getBillById(Long id) {
        BusBill bill = billMapper.selectById(id);
        if (bill == null) {
            throw new BusinessException("账单不存在");
        }
        return bill;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBill(BusBill bill) {
        if (bill.getAddressId() == null) {
            throw new BusinessException("住址不能为空");
        }
        if (bill.getPeriod() == null) {
            throw new BusinessException("账期不能为空");
        }
        calculateAndSetTotal(bill);
        billMapper.insert(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBill(BusBill bill) {
        if (bill.getAddressId() == null) {
            throw new BusinessException("住址不能为空");
        }
        if (bill.getPeriod() == null) {
            throw new BusinessException("账期不能为空");
        }
        calculateAndSetTotal(bill);
        billMapper.updateById(bill);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBill(Long id) {
        BusBill bill = billMapper.selectById(id);
        if (bill != null) {
            bill.setDelFlag(DEL_FLAG_DELETED);
            billMapper.updateById(bill);
        }
    }

    @Override
    public List<BusAttachment> getAttachments(Long billId) {
        return attachmentMapper.selectList(new LambdaQueryWrapper<BusAttachment>()
                .eq(BusAttachment::getBizType, BIZ_TYPE_BILL)
                .eq(BusAttachment::getBizId, billId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAttachment(BusAttachment attachment) {
        attachmentMapper.insert(attachment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long id) {
        attachmentMapper.deleteById(id);
    }

    private void calculateAndSetTotal(BusBill bill) {
        // 1. 有水费表底+单价则覆盖计算
        if (bill.getWaterPrevReading() != null && bill.getWaterCurrReading() != null && bill.getWaterUnitPrice() != null) {
            BigDecimal calculated = bill.getWaterCurrReading()
                    .subtract(bill.getWaterPrevReading())
                    .multiply(bill.getWaterUnitPrice())
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            bill.setWaterAmount(calculated);
        }

        // 2. 有电费表底+单价则覆盖计算
        if (bill.getElectricPrevReading() != null && bill.getElectricCurrReading() != null && bill.getElectricUnitPrice() != null) {
            BigDecimal calculated = bill.getElectricCurrReading()
                    .subtract(bill.getElectricPrevReading())
                    .multiply(bill.getElectricUnitPrice())
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            bill.setElectricAmount(calculated);
        }

        // 3. 损耗 = (水费+电费) × 5%，作为其他费用
        if (bill.getWaterAmount() != null && bill.getElectricAmount() != null) {
            BigDecimal loss = bill.getWaterAmount().add(bill.getElectricAmount())
                    .multiply(new BigDecimal("0.05"))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            bill.setOtherFee(loss);
        } else {
            bill.setOtherFee(BigDecimal.ZERO);
        }

        // 4. 计算各项小计
        BigDecimal sum = BigDecimal.ZERO;
        if (bill.getRent() != null) sum = sum.add(bill.getRent());
        if (bill.getWaterAmount() != null) sum = sum.add(bill.getWaterAmount());
        if (bill.getElectricAmount() != null) sum = sum.add(bill.getElectricAmount());
        if (bill.getManagementFee() != null) sum = sum.add(bill.getManagementFee());
        if (bill.getOtherFee() != null) sum = sum.add(bill.getOtherFee());

        // 5. 判断新增还是编辑
        if (bill.getTotalAmount() == null || bill.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            // 新增：合计 = 各项之和，抹零 = 0
            bill.setTotalAmount(sum);
            bill.setRoundingAmount(BigDecimal.ZERO);
        } else {
            // 编辑：合计保持不变，抹零 = 合计 - 各项之和
            BigDecimal rounding = bill.getTotalAmount().subtract(sum);
            bill.setRoundingAmount(rounding);
        }
    }
}
