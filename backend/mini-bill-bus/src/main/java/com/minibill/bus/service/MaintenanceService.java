package com.minibill.bus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.bus.entity.BusAttachment;
import com.minibill.bus.entity.BusMaintenance;

import java.util.List;

public interface MaintenanceService {

    Page<BusMaintenance> page(Integer pageNum, Integer pageSize, Long familyId, Long addressId, String type, String costDateStart, String costDateEnd);

    BusMaintenance getById(Long id);

    void add(BusMaintenance maintenance);

    void update(BusMaintenance maintenance);

    void delete(Long id);

    List<BusAttachment> getAttachments(Long maintenanceId);

    void addAttachment(BusAttachment attachment);

    void deleteAttachment(Long id);
}
