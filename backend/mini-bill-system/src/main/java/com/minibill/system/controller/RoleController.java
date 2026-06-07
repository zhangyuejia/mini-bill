package com.minibill.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.result.Result;
import com.minibill.system.entity.SysRole;
import com.minibill.system.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "分页查询角色")
    @GetMapping("/page")
    public Result<Page<SysRole>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) String keyword) {
        return Result.success(roleService.pageRole(pageNum, pageSize, keyword));
    }

    @Operation(summary = "查询所有角色")
    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.success(roleService.listAll());
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.success(roleService.getRoleById(id));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public Result<Void> add(@RequestBody SysRole role) {
        roleService.addRole(role);
        return Result.success();
    }

    @Operation(summary = "修改角色")
    @PutMapping
    public Result<Void> update(@RequestBody SysRole role) {
        roleService.updateRole(role);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    @Operation(summary = "获取角色菜单ID列表")
    @GetMapping("/{roleId}/menuIds")
    public Result<List<Long>> getMenuIds(@PathVariable Long roleId) {
        return Result.success(roleService.getRoleMenuIds(roleId));
    }

    @Operation(summary = "分配角色菜单")
    @PutMapping("/{roleId}/menus")
    public Result<Void> assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(roleId, menuIds);
        return Result.success();
    }
}
