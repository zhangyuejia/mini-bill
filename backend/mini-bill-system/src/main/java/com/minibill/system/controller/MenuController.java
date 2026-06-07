package com.minibill.system.controller;

import com.minibill.common.result.Result;
import com.minibill.system.entity.SysMenu;
import com.minibill.system.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "获取菜单树")
    @GetMapping("/tree")
    public Result<List<SysMenu>> getTree() {
        return Result.success(menuService.getMenuTree());
    }

    @Operation(summary = "获取用户菜单")
    @GetMapping("/userMenus")
    public Result<List<SysMenu>> getUserMenus(@RequestParam Long userId) {
        return Result.success(menuService.getUserMenus(userId));
    }

    @Operation(summary = "获取菜单详情")
    @GetMapping("/{id}")
    public Result<SysMenu> getById(@PathVariable Long id) {
        return Result.success(menuService.getMenuById(id));
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    public Result<Void> add(@RequestBody SysMenu menu) {
        menuService.addMenu(menu);
        return Result.success();
    }

    @Operation(summary = "修改菜单")
    @PutMapping
    public Result<Void> update(@RequestBody SysMenu menu) {
        menuService.updateMenu(menu);
        return Result.success();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success();
    }
}
