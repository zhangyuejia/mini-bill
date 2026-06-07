package com.minibill.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.result.Result;
import com.minibill.system.entity.SysUser;
import com.minibill.system.service.UserService;
import com.minibill.system.vo.PasswordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Result<Page<SysUser>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam(required = false) String keyword) {
        return Result.success(userService.pageUser(pageNum, pageSize, keyword));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @Operation(summary = "获取用户角色ID列表")
    @GetMapping("/{userId}/roleIds")
    public Result<List<Long>> getUserRoleIds(@PathVariable Long userId) {
        return Result.success(userService.getUserRoleIds(userId));
    }

    @Operation(summary = "分配用户角色")
    @PutMapping("/{userId}/roles")
    public Result<Void> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        userService.assignRoles(userId, roleIds);
        return Result.success();
    }

    @Operation(summary = "设置用户状态")
    @PutMapping("/{userId}/status")
    public Result<Void> setStatus(@PathVariable Long userId, @RequestParam Integer status) {
        userService.setUserStatus(userId, status);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PutMapping("/resetPassword")
    public Result<Void> resetPassword(@Valid @RequestBody PasswordVO vo) {
        userService.resetPassword(vo.getUserId(), vo.getNewPassword());
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/changePassword")
    public Result<Void> changePassword(@Valid @RequestBody PasswordVO vo) {
        userService.changePassword(vo.getUserId(), vo.getOldPassword(), vo.getNewPassword());
        return Result.success();
    }
}
