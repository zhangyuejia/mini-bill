package com.minibill.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.exception.BusinessException;
import com.minibill.bus.entity.BusItem;
import com.minibill.bus.entity.BusItemAttachment;
import com.minibill.bus.entity.BusItemCost;
import com.minibill.bus.entity.BusItemCostAttachment;
import com.minibill.bus.mapper.*;
import com.minibill.bus.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.minibill.common.constant.Constants.*;

/**
 * 物件管理服务实现
 */
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final BusItemMapper itemMapper;
    private final BusItemAttachmentMapper attachmentMapper;
    private final BusItemCostMapper costMapper;
    private final BusItemCostAttachmentMapper costAttachmentMapper;

    // ===== 物件 =====

    @Override
    public Page<BusItem> pageItem(Integer pageNum, Integer pageSize, Long familyId, Long addressId) {
        Page<BusItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BusItem> wrapper = new LambdaQueryWrapper<BusItem>()
                .eq(BusItem::getFamilyId, familyId)
                .eq(addressId != null, BusItem::getAddressId, addressId)
                .eq(BusItem::getDelFlag, DEL_FLAG_NORMAL)
                .orderByDesc(BusItem::getCreateTime);
        return itemMapper.selectPage(page, wrapper);
    }

    @Override
    public List<BusItem> listItemsByAddress(Long addressId) {
        return itemMapper.selectList(new LambdaQueryWrapper<BusItem>()
                .eq(BusItem::getAddressId, addressId)
                .eq(BusItem::getDelFlag, DEL_FLAG_NORMAL));
    }

    @Override
    public BusItem getItemById(Long id) {
        BusItem item = itemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException("物件不存在");
        }
        return item;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addItem(BusItem item) {
        itemMapper.insert(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(BusItem item) {
        itemMapper.updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long id) {
        BusItem item = itemMapper.selectById(id);
        if (item != null) {
            item.setDelFlag(DEL_FLAG_DELETED);
            itemMapper.updateById(item);
        }
    }

    @Override
    public List<BusItemAttachment> getItemAttachments(Long itemId) {
        return attachmentMapper.selectList(new LambdaQueryWrapper<BusItemAttachment>()
                .eq(BusItemAttachment::getItemId, itemId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addItemAttachment(BusItemAttachment attachment) {
        attachmentMapper.insert(attachment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItemAttachment(Long id) {
        attachmentMapper.deleteById(id);
    }

    // ===== 物件费用 =====

    @Override
    public Page<BusItemCost> pageItemCost(Integer pageNum, Integer pageSize, Long familyId, Long addressId, Long itemId, String costDateStart, String costDateEnd) {
        Page<BusItemCost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BusItemCost> wrapper = new LambdaQueryWrapper<BusItemCost>()
                .eq(BusItemCost::getFamilyId, familyId)
                .eq(addressId != null, BusItemCost::getAddressId, addressId)
                .eq(itemId != null, BusItemCost::getItemId, itemId)
                .ge(costDateStart != null, BusItemCost::getCostDate, costDateStart)
                .le(costDateEnd != null, BusItemCost::getCostDate, costDateEnd)
                .eq(BusItemCost::getDelFlag, DEL_FLAG_NORMAL)
                .orderByDesc(BusItemCost::getCostDate);
        return costMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addItemCost(BusItemCost cost) {
        costMapper.insert(cost);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItemCost(BusItemCost cost) {
        costMapper.updateById(cost);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItemCost(Long id) {
        BusItemCost cost = costMapper.selectById(id);
        if (cost != null) {
            cost.setDelFlag(DEL_FLAG_DELETED);
            costMapper.updateById(cost);
        }
    }

    @Override
    public List<BusItemCostAttachment> getCostAttachments(Long costId) {
        return costAttachmentMapper.selectList(new LambdaQueryWrapper<BusItemCostAttachment>()
                .eq(BusItemCostAttachment::getCostId, costId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCostAttachment(BusItemCostAttachment attachment) {
        costAttachmentMapper.insert(attachment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCostAttachment(Long id) {
        costAttachmentMapper.deleteById(id);
    }
}
