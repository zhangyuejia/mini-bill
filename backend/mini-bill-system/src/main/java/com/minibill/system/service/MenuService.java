package com.minibill.system.service;

import com.minibill.system.entity.SysMenu;

import java.util.List;

/**
 * 菜单管理服务接口
 */
public interface MenuService {

    List<SysMenu> getMenuTree();

    List<SysMenu> getUserMenus(Long userId);

    SysMenu getMenuById(Long id);

    void addMenu(SysMenu menu);

    void updateMenu(SysMenu menu);

    void deleteMenu(Long id);
}
