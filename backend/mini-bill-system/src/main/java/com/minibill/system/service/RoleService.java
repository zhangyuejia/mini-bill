package com.minibill.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.system.entity.SysRole;

import java.util.List;

/**
 * 角色管理服务接口
 */
public interface RoleService {

    Page<SysRole> pageRole(Integer pageNum, Integer pageSize, String keyword);

    List<SysRole> listAll();

    SysRole getRoleById(Long id);

    void addRole(SysRole role);

    void updateRole(SysRole role);

    void deleteRole(Long id);

    List<Long> getRoleMenuIds(Long roleId);

    void assignMenus(Long roleId, List<Long> menuIds);
}
