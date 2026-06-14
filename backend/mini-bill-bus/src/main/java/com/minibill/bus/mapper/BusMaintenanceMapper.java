package com.minibill.bus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.bus.entity.BusMaintenance;
import org.apache.ibatis.annotations.Param;

public interface BusMaintenanceMapper extends BaseMapper<BusMaintenance> {

    Page<BusMaintenance> pageWithAddress(Page<BusMaintenance> page,
                                         @Param("familyId") Long familyId,
                                         @Param("addressId") Long addressId,
                                         @Param("type") String type,
                                         @Param("costDateStart") String costDateStart,
                                         @Param("costDateEnd") String costDateEnd);
}
