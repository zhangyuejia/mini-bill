package com.minibill.bus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.bus.entity.BusAttachment;
import com.minibill.bus.entity.BusBill;

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

    List<BusAttachment> getAttachments(Long billId);

    void addAttachment(BusAttachment attachment);

    void deleteAttachment(Long id);
}
