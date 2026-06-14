package com.minibill.bus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.bus.dto.ItemPageQuery;
import com.minibill.bus.entity.BusItem;
import org.apache.ibatis.annotations.Param;

public interface BusItemMapper extends BaseMapper<BusItem> {

    Page<BusItem> pageItemWithAddress(Page<BusItem> page, @Param("q") ItemPageQuery q);
}
