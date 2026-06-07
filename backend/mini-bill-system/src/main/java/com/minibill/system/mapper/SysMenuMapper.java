package com.minibill.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minibill.system.entity.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> getMenusByUserId(@Param("userId") Long userId);
}
