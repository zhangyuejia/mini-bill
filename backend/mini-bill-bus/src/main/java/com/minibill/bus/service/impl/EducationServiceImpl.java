package com.minibill.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.exception.BusinessException;
import com.minibill.bus.entity.BusAttachment;
import com.minibill.bus.entity.BusEducation;
import com.minibill.bus.entity.BusEducationItem;
import com.minibill.bus.mapper.BusAttachmentMapper;
import com.minibill.bus.mapper.BusEducationItemMapper;
import com.minibill.bus.mapper.BusEducationMapper;
import com.minibill.bus.service.EducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.minibill.common.constant.Constants.*;

@Service
@RequiredArgsConstructor
public class EducationServiceImpl implements EducationService {

    private final BusEducationMapper educationMapper;
    private final BusEducationItemMapper itemMapper;
    private final BusAttachmentMapper attachmentMapper;

    @Override
    public Page<BusEducation> page(Integer pageNum, Integer pageSize, Long familyId, Long memberId, String semesterDateStart, String semesterDateEnd) {
        Page<BusEducation> page = new Page<>(pageNum, pageSize);
        Page<BusEducation> result = educationMapper.pageWithMember(page, familyId, memberId, semesterDateStart, semesterDateEnd);

        List<BusEducation> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Long> eduIds = records.stream().map(BusEducation::getId).collect(Collectors.toList());

            // 批量查明细
            List<BusEducationItem> allItems = itemMapper.selectList(
                    new LambdaQueryWrapper<BusEducationItem>()
                            .in(BusEducationItem::getEducationId, eduIds));
            Map<Long, List<BusEducationItem>> itemMap = allItems.stream()
                    .collect(Collectors.groupingBy(BusEducationItem::getEducationId));

            // 批量查明细附件
            List<Long> itemIds = allItems.stream().map(BusEducationItem::getId).collect(Collectors.toList());
            List<BusAttachment> itemAttachments = itemIds.isEmpty() ? List.of() :
                    attachmentMapper.selectList(new LambdaQueryWrapper<BusAttachment>()
                            .eq(BusAttachment::getBizType, BIZ_TYPE_EDUCATION_ITEM)
                            .in(BusAttachment::getBizId, itemIds));
            Map<Long, List<BusAttachment>> itemAttMap = itemAttachments.stream()
                    .collect(Collectors.groupingBy(BusAttachment::getBizId));
            allItems.forEach(item -> item.setAttachments(itemAttMap.getOrDefault(item.getId(), new ArrayList<>())));

            // 批量查主表附件
            List<BusAttachment> allAttachments = attachmentMapper.selectList(
                    new LambdaQueryWrapper<BusAttachment>()
                            .eq(BusAttachment::getBizType, BIZ_TYPE_EDUCATION)
                            .in(BusAttachment::getBizId, eduIds));
            Map<Long, List<BusAttachment>> attMap = allAttachments.stream()
                    .collect(Collectors.groupingBy(BusAttachment::getBizId));
            records.forEach(e -> {
                e.setItems(itemMap.getOrDefault(e.getId(), new ArrayList<>()));
                e.setAttachments(attMap.getOrDefault(e.getId(), new ArrayList<>()));
            });
        }

        return result;
    }

    @Override
    public BusEducation getById(Long id) {
        BusEducation e = educationMapper.selectById(id);
        if (e == null) throw new BusinessException("教育费用记录不存在");
        e.setItems(itemMapper.selectList(new LambdaQueryWrapper<BusEducationItem>()
                .eq(BusEducationItem::getEducationId, id)));
        return e;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(BusEducation education, List<BusEducationItem> items) {
        educationMapper.insert(education);
        if (items != null) {
            items.forEach(item -> {
                item.setEducationId(education.getId());
                itemMapper.insert(item);
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(BusEducation education, List<BusEducationItem> items) {
        educationMapper.updateById(education);
        itemMapper.delete(new LambdaQueryWrapper<BusEducationItem>()
                .eq(BusEducationItem::getEducationId, education.getId()));
        if (items != null) {
            items.forEach(item -> {
                item.setId(null);
                item.setEducationId(education.getId());
                itemMapper.insert(item);
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        BusEducation e = educationMapper.selectById(id);
        if (e != null) {
            e.setDelFlag(DEL_FLAG_DELETED);
            educationMapper.updateById(e);
        }
    }

    @Override
    public List<BusAttachment> getAttachments(Long educationId) {
        return attachmentMapper.selectList(new LambdaQueryWrapper<BusAttachment>()
                .eq(BusAttachment::getBizType, BIZ_TYPE_EDUCATION)
                .eq(BusAttachment::getBizId, educationId));
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

    // ===== 明细CRUD =====

    @Override
    public List<BusEducationItem> getItems(Long educationId) {
        List<BusEducationItem> items = itemMapper.selectList(new LambdaQueryWrapper<BusEducationItem>()
                .eq(BusEducationItem::getEducationId, educationId));
        if (!items.isEmpty()) {
            List<Long> itemIds = items.stream().map(BusEducationItem::getId).collect(Collectors.toList());
            List<BusAttachment> atts = itemIds.isEmpty() ? List.of() :
                    attachmentMapper.selectList(new LambdaQueryWrapper<BusAttachment>()
                            .eq(BusAttachment::getBizType, BIZ_TYPE_EDUCATION_ITEM)
                            .in(BusAttachment::getBizId, itemIds));
            Map<Long, List<BusAttachment>> attMap = atts.stream()
                    .collect(Collectors.groupingBy(BusAttachment::getBizId));
            items.forEach(item -> item.setAttachments(attMap.getOrDefault(item.getId(), new ArrayList<>())));
        }
        return items;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BusEducationItem addItem(BusEducationItem item) {
        itemMapper.insert(item);
        return item;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItem(BusEducationItem item) {
        itemMapper.updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(Long id) {
        itemMapper.deleteById(id);
    }

    @Override
    public List<BusAttachment> getItemAttachments(Long itemId) {
        return attachmentMapper.selectList(new LambdaQueryWrapper<BusAttachment>()
                .eq(BusAttachment::getBizType, BIZ_TYPE_EDUCATION_ITEM)
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
}
