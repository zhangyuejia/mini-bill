package com.minibill.bus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.bus.entity.BusAttachment;
import com.minibill.bus.entity.BusEducation;
import com.minibill.bus.entity.BusEducationItem;

import java.util.List;

public interface EducationService {

    Page<BusEducation> page(Integer pageNum, Integer pageSize, Long familyId, Long memberId, String semesterDateStart, String semesterDateEnd);

    BusEducation getById(Long id);

    void add(BusEducation education, List<BusEducationItem> items);

    void update(BusEducation education, List<BusEducationItem> items);

    void delete(Long id);

    List<BusAttachment> getAttachments(Long educationId);

    void addAttachment(BusAttachment attachment);

    void deleteAttachment(Long id);

    // 明细CRUD
    List<BusEducationItem> getItems(Long educationId);

    BusEducationItem addItem(BusEducationItem item);

    void updateItem(BusEducationItem item);

    void deleteItem(Long id);

    // 明细附件
    List<BusAttachment> getItemAttachments(Long itemId);

    void addItemAttachment(BusAttachment attachment);

    void deleteItemAttachment(Long id);
}
