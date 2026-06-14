package com.minibill.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.bus.dto.ItemPageQuery;
import com.minibill.common.exception.BusinessException;
import com.minibill.bus.entity.BusAttachment;
import com.minibill.bus.entity.BusItem;
import com.minibill.bus.entity.BusItemCost;
import com.minibill.bus.mapper.*;
import com.minibill.bus.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.minibill.common.constant.Constants.*;

/**
 * 物件管理服务实现
 */
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final BusItemMapper itemMapper;
    private final BusItemCostMapper costMapper;
    private final BusAttachmentMapper attachmentMapper;

    // ===== 物件 =====

    @Override
    public Page<BusItem> pageItem(ItemPageQuery q) {
        Page<BusItem> page = new Page<>(q.getPageNum(), q.getPageSize());
        // LEFT JOIN bus_address 一次查出住址名称
        Page<BusItem> result = itemMapper.pageItemWithAddress(page, q);

        // 批量查询附件（1:N 无法用 JOIN 折叠）
        List<BusItem> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Long> itemIds = records.stream().map(BusItem::getId).collect(Collectors.toList());
            List<BusAttachment> allAttachments = attachmentMapper.selectList(
                    new LambdaQueryWrapper<BusAttachment>()
                            .eq(BusAttachment::getBizType, BIZ_TYPE_ITEM)
                            .in(BusAttachment::getBizId, itemIds));
            Map<Long, List<BusAttachment>> attachmentMap = allAttachments.stream()
                    .collect(Collectors.groupingBy(BusAttachment::getBizId));
            records.forEach(item -> item.setAttachments(attachmentMap.getOrDefault(item.getId(), new ArrayList<>())));
        }

        return result;
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
    public List<BusAttachment> getItemAttachments(Long itemId) {
        return attachmentMapper.selectList(new LambdaQueryWrapper<BusAttachment>()
                .eq(BusAttachment::getBizType, BIZ_TYPE_ITEM)
                .eq(BusAttachment::getBizId, itemId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addItemAttachment(BusAttachment attachment) {
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
        // LEFT JOIN bus_item 一次查出物件名称，family/address 通过 JOIN 的 item 表过滤
        Page<BusItemCost> result = costMapper.pageCostWithItem(page, familyId, addressId, itemId, costDateStart, costDateEnd);

        // 批量查询附件
        List<BusItemCost> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Long> costIds = records.stream().map(BusItemCost::getId).collect(Collectors.toList());
            List<BusAttachment> allAttachments = attachmentMapper.selectList(
                    new LambdaQueryWrapper<BusAttachment>()
                            .eq(BusAttachment::getBizType, BIZ_TYPE_ITEM_COST)
                            .in(BusAttachment::getBizId, costIds));
            Map<Long, List<BusAttachment>> attachmentMap = allAttachments.stream()
                    .collect(Collectors.groupingBy(BusAttachment::getBizId));
            records.forEach(cost -> cost.setAttachments(attachmentMap.getOrDefault(cost.getId(), new ArrayList<>())));
        }

        return result;
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
    public List<BusAttachment> getCostAttachments(Long costId) {
        return attachmentMapper.selectList(new LambdaQueryWrapper<BusAttachment>()
                .eq(BusAttachment::getBizType, BIZ_TYPE_ITEM_COST)
                .eq(BusAttachment::getBizId, costId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCostAttachment(BusAttachment attachment) {
        attachmentMapper.insert(attachment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCostAttachment(Long id) {
        attachmentMapper.deleteById(id);
    }
}
