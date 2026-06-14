package com.minibill.bus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.bus.entity.BusItemCost;
import org.apache.ibatis.annotations.Param;

public interface BusItemCostMapper extends BaseMapper<BusItemCost> {

    Page<BusItemCost> pageCostWithItem(Page<BusItemCost> page,
                                       @Param("familyId") Long familyId,
                                       @Param("addressId") Long addressId,
                                       @Param("itemId") Long itemId,
                                       @Param("costDateStart") String costDateStart,
                                       @Param("costDateEnd") String costDateEnd);
}
