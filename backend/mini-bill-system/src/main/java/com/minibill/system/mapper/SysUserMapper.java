package com.minibill.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minibill.system.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysUserMapper extends BaseMapper<SysUser> {

    List<String> getRoleCodesByUserId(@Param("userId") Long userId);

    List<String> getPermissionsByUserId(@Param("userId") Long userId);

    boolean hasRole(@Param("userId") Long userId, @Param("roleCode") String roleCode);
}
