package com.minibill.bus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.result.Result;
import com.minibill.bus.entity.BusFamily;
import com.minibill.bus.entity.BusFamilyMember;
import com.minibill.bus.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 家庭管理控制器
 */
@Tag(name = "家庭管理")
@RestController
@RequestMapping("/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @Operation(summary = "获取用户的家庭列表")
    @GetMapping("/my")
    public Result<List<BusFamily>> getMyFamilies(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(familyService.getUserFamilies(userId));
    }

    @Operation(summary = "创建家庭")
    @PostMapping
    public Result<BusFamily> create(@RequestHeader("X-User-Id") Long userId,
                                    @RequestBody Map<String, String> body) {
        return Result.success(familyService.createFamily(userId, body.get("name")));
    }

    @Operation(summary = "邀请成员（通过邮箱）")
    @PostMapping("/{familyId}/invite")
    public Result<Void> invite(@RequestHeader("X-User-Id") Long userId,
                               @PathVariable Long familyId,
                               @RequestBody Map<String, String> body) {
        familyService.inviteMember(familyId, userId, body.get("email"));
        return Result.success();
    }

    @Operation(summary = "移除成员")
    @DeleteMapping("/{familyId}/member/{targetUserId}")
    public Result<Void> removeMember(@RequestHeader("X-User-Id") Long userId,
                                     @PathVariable Long familyId,
                                     @PathVariable Long targetUserId) {
        familyService.removeMember(familyId, userId, targetUserId);
        return Result.success();
    }

    @Operation(summary = "获取家庭成员列表")
    @GetMapping("/{familyId}/members")
    public Result<List<BusFamilyMember>> getMembers(@PathVariable Long familyId) {
        return Result.success(familyService.getFamilyMembers(familyId));
    }

    @Operation(summary = "设置默认家庭")
    @PutMapping("/{familyId}/default")
    public Result<Void> setDefault(@RequestHeader("X-User-Id") Long userId,
                                   @PathVariable Long familyId) {
        familyService.setDefaultFamily(userId, familyId);
        return Result.success();
    }

    @Operation(summary = "分页查询家庭")
    @GetMapping("/page")
    public Result<Page<BusFamily>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) String keyword) {
        return Result.success(familyService.pageFamily(pageNum, pageSize, keyword));
    }

    @Operation(summary = "修改家庭信息")
    @PutMapping
    public Result<Void> update(@RequestBody BusFamily family) {
        familyService.updateFamily(family);
        return Result.success();
    }

    @Operation(summary = "获取家庭详情")
    @GetMapping("/{id}")
    public Result<BusFamily> getById(@PathVariable Long id) {
        return Result.success(familyService.getFamilyById(id));
    }
}
