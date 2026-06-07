package com.minibill.bus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.result.Result;
import com.minibill.bus.entity.BusFamilySaving;
import com.minibill.bus.entity.BusSavingItem;
import com.minibill.bus.entity.BusSavingRecord;
import com.minibill.bus.service.SavingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 储蓄管理控制器
 */
@Tag(name = "储蓄管理")
@RestController
@RequestMapping("/saving")
@RequiredArgsConstructor
public class SavingController {

    private final SavingService savingService;

    // ===== 储蓄项 =====

    @Operation(summary = "获取家庭成员储蓄项")
    @GetMapping("/items/{familyId}/{memberId}")
    public Result<List<BusSavingItem>> getItems(@PathVariable Long familyId, @PathVariable Long memberId) {
        return Result.success(savingService.getSavingItemsByMember(familyId, memberId));
    }

    @Operation(summary = "新增储蓄项")
    @PostMapping("/item")
    public Result<Void> addItem(@RequestBody BusSavingItem item) {
        savingService.addSavingItem(item);
        return Result.success();
    }

    @Operation(summary = "修改储蓄项")
    @PutMapping("/item")
    public Result<Void> updateItem(@RequestBody BusSavingItem item) {
        savingService.updateSavingItem(item);
        return Result.success();
    }

    @Operation(summary = "删除储蓄项")
    @DeleteMapping("/item/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        savingService.deleteSavingItem(id);
        return Result.success();
    }

    // ===== 家庭储蓄 =====

    @Operation(summary = "分页查询家庭储蓄")
    @GetMapping("/page")
    public Result<Page<BusFamilySaving>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam Long familyId,
                                              @RequestParam(required = false) String savingDateStart,
                                              @RequestParam(required = false) String savingDateEnd) {
        return Result.success(savingService.pageFamilySaving(pageNum, pageSize, familyId, savingDateStart, savingDateEnd));
    }

    @Operation(summary = "创建家庭储蓄")
    @PostMapping("/create")
    public Result<BusFamilySaving> create(@RequestBody BusFamilySaving saving) {
        return Result.success(savingService.createFamilySaving(saving));
    }

    @Operation(summary = "修改家庭储蓄")
    @PutMapping
    public Result<Void> update(@RequestBody BusFamilySaving saving) {
        savingService.updateFamilySaving(saving);
        return Result.success();
    }

    @Operation(summary = "删除家庭储蓄")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        savingService.deleteFamilySaving(id);
        return Result.success();
    }

    @Operation(summary = "批量保存储蓄记录")
    @PostMapping("/records/{savingId}")
    public Result<Void> batchSaveRecords(@PathVariable Long savingId,
                                         @RequestBody List<BusSavingRecord> records) {
        savingService.batchSaveRecords(savingId, records);
        return Result.success();
    }

    @Operation(summary = "获取储蓄记录")
    @GetMapping("/records/{savingId}")
    public Result<List<BusSavingRecord>> getRecords(@PathVariable Long savingId) {
        return Result.success(savingService.getSavingRecords(savingId));
    }
}
