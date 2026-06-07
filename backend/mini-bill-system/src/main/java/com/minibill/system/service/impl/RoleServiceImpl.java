package com.minibill.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.exception.BusinessException;
import com.minibill.system.entity.SysRole;
import com.minibill.system.entity.SysRoleMenu;
import com.minibill.system.mapper.SysRoleMapper;
import com.minibill.system.mapper.SysRoleMenuMapper;
import com.minibill.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.minibill.common.constant.Constants.*;

/**
 * 角色管理服务实现
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    @Override
    public Page<SysRole> pageRole(Integer pageNum, Integer pageSize, String keyword) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDelFlag, DEL_FLAG_NORMAL)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(SysRole::getName, keyword)
                        .or()
                        .like(SysRole::getCode, keyword))
                .orderByAsc(SysRole::getSort);
        return roleMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysRole> listAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDelFlag, DEL_FLAG_NORMAL)
                .eq(SysRole::getStatus, STATUS_ENABLE)
                .orderByAsc(SysRole::getSort));
    }

    @Override
    public SysRole getRoleById(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(SysRole role) {
        Long count = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCode, role.getCode())
                .eq(SysRole::getDelFlag, DEL_FLAG_NORMAL));
        if (count > 0) {
            throw new BusinessException("角色编码已存在");
        }
        roleMapper.insert(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(SysRole role) {
        SysRole exist = roleMapper.selectById(role.getId());
        if (exist == null) {
            throw new BusinessException("角色不存在");
        }
        roleMapper.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        role.setDelFlag(DEL_FLAG_DELETED);
        roleMapper.updateById(role);
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return roleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>()
                                .eq(SysRoleMenu::getRoleId, roleId))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
    }
}
