package com.minibill.bus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.bus.entity.BusFamilySaving;
import com.minibill.bus.entity.BusSavingItem;
import com.minibill.bus.entity.BusSavingRecord;

import java.util.List;

/**
 * 储蓄管理服务接口
 */
public interface SavingService {

    // ===== 储蓄项 =====

    List<BusSavingItem> getSavingItemsByMember(Long familyId, Long memberId);

    void addSavingItem(BusSavingItem item);

    void updateSavingItem(BusSavingItem item);

    void deleteSavingItem(Long id);

    // ===== 家庭储蓄 =====

    Page<BusFamilySaving> pageFamilySaving(Integer pageNum, Integer pageSize,
                                           Long familyId, String savingDateStart, String savingDateEnd);

    BusFamilySaving createFamilySaving(BusFamilySaving saving);

    void updateFamilySaving(BusFamilySaving saving);

    void deleteFamilySaving(Long id);

    void addSavingRecord(BusSavingRecord record);

    void batchSaveRecords(Long savingId, List<BusSavingRecord> records);

    List<BusSavingRecord> getSavingRecords(Long savingId);
}
