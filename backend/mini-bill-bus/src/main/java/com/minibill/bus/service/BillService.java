package com.minibill.bus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.bus.entity.BusBill;
import com.minibill.bus.entity.BusBillAttachment;

import java.util.List;

/**
 * 账单管理服务接口
 */
public interface BillService {

    Page<BusBill> pageBill(Integer pageNum, Integer pageSize, Long familyId, Long addressId, Integer periodStart, Integer periodEnd);

    BusBill getBillById(Long id);

    void addBill(BusBill bill);

    void updateBill(BusBill bill);

    void deleteBill(Long id);

    List<BusBillAttachment> getAttachments(Long billId);

    void addAttachment(BusBillAttachment attachment);

    void deleteAttachment(Long id);

    /**
     * 迁移历史账单：从备注中提取管理费，分摊到 managementFee 和 otherFee
     * @return 迁移的记录数
     */
    int migrateManagementFee();
}
