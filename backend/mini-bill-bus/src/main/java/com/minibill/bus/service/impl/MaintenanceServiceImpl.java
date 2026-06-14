package com.minibill.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.exception.BusinessException;
import com.minibill.bus.entity.BusAttachment;
import com.minibill.bus.entity.BusMaintenance;
import com.minibill.bus.mapper.BusAttachmentMapper;
import com.minibill.bus.mapper.BusMaintenanceMapper;
import com.minibill.bus.service.MaintenanceService;
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
public class MaintenanceServiceImpl implements MaintenanceService {

    private final BusMaintenanceMapper maintenanceMapper;
    private final BusAttachmentMapper attachmentMapper;

    @Override
    public Page<BusMaintenance> page(Integer pageNum, Integer pageSize, Long familyId, Long addressId, String type, String costDateStart, String costDateEnd) {
        Page<BusMaintenance> page = new Page<>(pageNum, pageSize);
        Page<BusMaintenance> result = maintenanceMapper.pageWithAddress(page, familyId, addressId, type, costDateStart, costDateEnd);

        List<BusMaintenance> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Long> ids = records.stream().map(BusMaintenance::getId).collect(Collectors.toList());
            List<BusAttachment> allAttachments = attachmentMapper.selectList(
                    new LambdaQueryWrapper<BusAttachment>()
                            .eq(BusAttachment::getBizType, BIZ_TYPE_MAINTENANCE)
                            .in(BusAttachment::getBizId, ids));
            Map<Long, List<BusAttachment>> attachmentMap = allAttachments.stream()
                    .collect(Collectors.groupingBy(BusAttachment::getBizId));
            records.forEach(m -> m.setAttachments(attachmentMap.getOrDefault(m.getId(), new ArrayList<>())));
        }

        return result;
    }

    @Override
    public BusMaintenance getById(Long id) {
        BusMaintenance m = maintenanceMapper.selectById(id);
        if (m == null) throw new BusinessException("维护费用记录不存在");
        return m;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(BusMaintenance maintenance) {
        maintenanceMapper.insert(maintenance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(BusMaintenance maintenance) {
        maintenanceMapper.updateById(maintenance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        BusMaintenance m = maintenanceMapper.selectById(id);
        if (m != null) {
            m.setDelFlag(DEL_FLAG_DELETED);
            maintenanceMapper.updateById(m);
        }
    }

    @Override
    public List<BusAttachment> getAttachments(Long maintenanceId) {
        return attachmentMapper.selectList(new LambdaQueryWrapper<BusAttachment>()
                .eq(BusAttachment::getBizType, BIZ_TYPE_MAINTENANCE)
                .eq(BusAttachment::getBizId, maintenanceId));
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
}
