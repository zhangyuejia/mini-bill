package com.minibill.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minibill.common.exception.BusinessException;
import com.minibill.system.entity.SysMenu;
import com.minibill.system.mapper.SysMenuMapper;
import com.minibill.system.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.minibill.common.constant.Constants.DEL_FLAG_DELETED;
import static com.minibill.common.constant.Constants.DEL_FLAG_NORMAL;

/**
 * 菜单管理服务实现
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final SysMenuMapper menuMapper;

    @Override
    public List<SysMenu> getMenuTree() {
        List<SysMenu> allMenus = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getDelFlag, DEL_FLAG_NORMAL)
                .orderByAsc(SysMenu::getSort));
        return buildTree(allMenus);
    }

    @Override
    public List<SysMenu> getUserMenus(Long userId) {
        List<SysMenu> menus = menuMapper.getMenusByUserId(userId);
        return buildTree(menus);
    }

    @Override
    public SysMenu getMenuById(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        return menu;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMenu(SysMenu menu) {
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        menuMapper.insert(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(SysMenu menu) {
        SysMenu exist = menuMapper.selectById(menu.getId());
        if (exist == null) {
            throw new BusinessException("菜单不存在");
        }
        menuMapper.updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        Long childCount = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id)
                .eq(SysMenu::getDelFlag, DEL_FLAG_NORMAL));
        if (childCount > 0) {
            throw new BusinessException("请先删除子菜单");
        }
        SysMenu menu = menuMapper.selectById(id);
        if (menu != null) {
            menu.setDelFlag(DEL_FLAG_DELETED);
            menuMapper.updateById(menu);
        }
    }

    private List<SysMenu> buildTree(List<SysMenu> menus) {
        List<SysMenu> sorted = menus.stream()
                .sorted(Comparator.comparingInt(SysMenu::getSort))
                .collect(Collectors.toList());

        return sorted.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .map(m -> {
                    m.setChildren(getChildren(m.getId(), sorted));
                    return m;
                })
                .collect(Collectors.toList());
    }

    private List<SysMenu> getChildren(Long parentId, List<SysMenu> allMenus) {
        return allMenus.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .map(m -> {
                    m.setChildren(getChildren(m.getId(), allMenus));
                    return m;
                })
                .collect(Collectors.toList());
    }
}
