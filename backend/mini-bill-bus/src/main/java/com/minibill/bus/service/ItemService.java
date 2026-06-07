package com.minibill.bus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.bus.entity.BusItem;
import com.minibill.bus.entity.BusItemAttachment;
import com.minibill.bus.entity.BusItemCost;
import com.minibill.bus.entity.BusItemCostAttachment;

import java.util.List;

/**
 * 物件管理服务接口
 */
public interface ItemService {

    // ===== 物件 =====

    Page<BusItem> pageItem(Integer pageNum, Integer pageSize, Long familyId, Long addressId);

    List<BusItem> listItemsByAddress(Long addressId);

    BusItem getItemById(Long id);

    void addItem(BusItem item);

    void updateItem(BusItem item);

    void deleteItem(Long id);

    List<BusItemAttachment> getItemAttachments(Long itemId);

    void addItemAttachment(BusItemAttachment attachment);

    void deleteItemAttachment(Long id);

    // ===== 物件费用 =====

    Page<BusItemCost> pageItemCost(Integer pageNum, Integer pageSize, Long familyId, Long addressId, Long itemId, String costDateStart, String costDateEnd);

    void addItemCost(BusItemCost cost);

    void updateItemCost(BusItemCost cost);

    void deleteItemCost(Long id);

    List<BusItemCostAttachment> getCostAttachments(Long costId);

    void addCostAttachment(BusItemCostAttachment attachment);

    void deleteCostAttachment(Long id);
}
