package com.minibill.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.exception.BusinessException;
import com.minibill.bus.entity.BusFamilySaving;
import com.minibill.bus.entity.BusSavingItem;
import com.minibill.bus.entity.BusSavingRecord;
import com.minibill.bus.mapper.BusFamilySavingMapper;
import com.minibill.bus.mapper.BusSavingItemMapper;
import com.minibill.bus.mapper.BusSavingRecordMapper;
import com.minibill.bus.service.SavingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

import static com.minibill.common.constant.Constants.*;

/**
 * 储蓄管理服务实现
 */
@Service
@RequiredArgsConstructor
public class SavingServiceImpl implements SavingService {

    private final BusSavingItemMapper savingItemMapper;
    private final BusFamilySavingMapper familySavingMapper;
    private final BusSavingRecordMapper savingRecordMapper;

    // ===== 储蓄项 =====

    @Override
    public List<BusSavingItem> getSavingItemsByMember(Long familyId, Long memberId) {
        LambdaQueryWrapper<BusSavingItem> wrapper = new LambdaQueryWrapper<BusSavingItem>()
                .eq(BusSavingItem::getFamilyId, familyId)
                .eq(BusSavingItem::getDelFlag, DEL_FLAG_NORMAL)
                .eq(BusSavingItem::getStatus, STATUS_ENABLE);
        // memberId=0 查全部，否则查指定成员
        if (memberId != null && memberId != 0) {
            wrapper.eq(BusSavingItem::getMemberId, memberId);
        }
        return savingItemMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSavingItem(BusSavingItem item) {
        Long count = savingItemMapper.selectCount(new LambdaQueryWrapper<BusSavingItem>()
                .eq(BusSavingItem::getFamilyId, item.getFamilyId())
                .eq(BusSavingItem::getMemberId, item.getMemberId())
                .eq(BusSavingItem::getName, item.getName())
                .eq(BusSavingItem::getDelFlag, DEL_FLAG_NORMAL));
        if (count > 0) {
            throw new BusinessException("该成员已存在同名储蓄项");
        }
        item.setStatus(STATUS_ENABLE);
        savingItemMapper.insert(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSavingItem(BusSavingItem item) {
        savingItemMapper.updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSavingItem(Long id) {
        BusSavingItem item = savingItemMapper.selectById(id);
        if (item != null) {
            item.setDelFlag(DEL_FLAG_DELETED);
            savingItemMapper.updateById(item);
        }
    }

    // ===== 家庭储蓄 =====

    @Override
    public Page<BusFamilySaving> pageFamilySaving(Integer pageNum, Integer pageSize,
                                                  Long familyId, String savingDateStart, String savingDateEnd) {
        Page<BusFamilySaving> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BusFamilySaving> wrapper = new LambdaQueryWrapper<BusFamilySaving>()
                .eq(BusFamilySaving::getFamilyId, familyId)
                .ge(StringUtils.hasText(savingDateStart), BusFamilySaving::getSavingDate, StringUtils.hasText(savingDateStart) ? LocalDate.parse(savingDateStart) : null)
                .le(StringUtils.hasText(savingDateEnd), BusFamilySaving::getSavingDate, StringUtils.hasText(savingDateEnd) ? LocalDate.parse(savingDateEnd) : null)
                .eq(BusFamilySaving::getDelFlag, DEL_FLAG_NORMAL)
                .orderByDesc(BusFamilySaving::getSavingDate);
        Page<BusFamilySaving> result = familySavingMapper.selectPage(page, wrapper);

        // 批量查询 records，消除 N+1
        List<BusFamilySaving> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Long> savingIds = records.stream().map(BusFamilySaving::getId).collect(Collectors.toList());
            List<BusSavingRecord> allRecords = savingRecordMapper.selectList(
                    new LambdaQueryWrapper<BusSavingRecord>()
                            .in(BusSavingRecord::getSavingId, savingIds));
            Map<Long, List<BusSavingRecord>> recordMap = allRecords.stream()
                    .collect(Collectors.groupingBy(BusSavingRecord::getSavingId));
            records.forEach(saving -> saving.setRecords(recordMap.getOrDefault(saving.getId(), new ArrayList<>())));
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BusFamilySaving createFamilySaving(BusFamilySaving saving) {
        saving.setTotalAmount(BigDecimal.ZERO);
        familySavingMapper.insert(saving);
        return saving;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFamilySaving(BusFamilySaving saving) {
        BusFamilySaving exist = familySavingMapper.selectById(saving.getId());
        if (exist == null) throw new BusinessException("储蓄记录不存在");
        if (saving.getSavingDate() != null) exist.setSavingDate(saving.getSavingDate());
        familySavingMapper.updateById(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFamilySaving(Long id) {
        BusFamilySaving saving = familySavingMapper.selectById(id);
        if (saving != null) {
            saving.setDelFlag(DEL_FLAG_DELETED);
            familySavingMapper.updateById(saving);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSavingRecord(BusSavingRecord record) {
        savingRecordMapper.insert(record);
        recalcTotal(record.getSavingId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveRecords(Long savingId, List<BusSavingRecord> records) {
        savingRecordMapper.delete(new LambdaQueryWrapper<BusSavingRecord>()
                .eq(BusSavingRecord::getSavingId, savingId));

        if (records != null && !records.isEmpty()) {
            for (BusSavingRecord record : records) {
                record.setSavingId(savingId);
                savingRecordMapper.insert(record);
            }
        }
        recalcTotal(savingId);
    }

    @Override
    public List<BusSavingRecord> getSavingRecords(Long savingId) {
        return savingRecordMapper.selectList(new LambdaQueryWrapper<BusSavingRecord>()
                .eq(BusSavingRecord::getSavingId, savingId));
    }

    private void recalcTotal(Long savingId) {
        List<BusSavingRecord> records = savingRecordMapper.selectList(
                new LambdaQueryWrapper<BusSavingRecord>()
                        .eq(BusSavingRecord::getSavingId, savingId));

        BigDecimal total = records.stream()
                .map(r -> r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BusFamilySaving saving = familySavingMapper.selectById(savingId);
        if (saving != null) {
            saving.setTotalAmount(total);
            familySavingMapper.updateById(saving);
        }
    }
}
