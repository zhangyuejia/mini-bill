package com.minibill.bus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.result.Result;
import com.minibill.bus.entity.*;
import com.minibill.bus.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物件管理控制器
 */
@Tag(name = "物件管理")
@RestController
@RequestMapping("/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // ===== 物件 =====

    @Operation(summary = "分页查询物件")
    @GetMapping("/page")
    public Result<Page<BusItem>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam Long familyId,
                                      @RequestParam(required = false) Long addressId) {
        return Result.success(itemService.pageItem(pageNum, pageSize, familyId, addressId));
    }

    @Operation(summary = "根据住址获取物件列表")
    @GetMapping("/listByAddress/{addressId}")
    public Result<List<BusItem>> listByAddress(@PathVariable Long addressId) {
        return Result.success(itemService.listItemsByAddress(addressId));
    }

    @Operation(summary = "获取物件详情")
    @GetMapping("/{id}")
    public Result<BusItem> getById(@PathVariable Long id) {
        return Result.success(itemService.getItemById(id));
    }

    @Operation(summary = "新增物件")
    @PostMapping
    public Result<BusItem> add(@RequestBody BusItem item) {
        itemService.addItem(item);
        return Result.success(item);
    }

    @Operation(summary = "修改物件")
    @PutMapping
    public Result<Void> update(@RequestBody BusItem item) {
        itemService.updateItem(item);
        return Result.success();
    }

    @Operation(summary = "删除物件")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        itemService.deleteItem(id);
        return Result.success();
    }

    @Operation(summary = "获取物件附件")
    @GetMapping("/{itemId}/attachments")
    public Result<List<BusItemAttachment>> getAttachments(@PathVariable Long itemId) {
        return Result.success(itemService.getItemAttachments(itemId));
    }

    @Operation(summary = "添加物件附件")
    @PostMapping("/{itemId}/attachment")
    public Result<Void> addAttachment(@PathVariable Long itemId, @RequestBody BusItemAttachment attachment) {
        attachment.setItemId(itemId);
        attachment.setId(null);
        itemService.addItemAttachment(attachment);
        return Result.success();
    }

    @Operation(summary = "删除物件附件")
    @DeleteMapping("/attachment/{id}")
    public Result<Void> deleteAttachment(@PathVariable Long id) {
        itemService.deleteItemAttachment(id);
        return Result.success();
    }

    // ===== 物件费用 =====

    @Operation(summary = "分页查询物件费用")
    @GetMapping("/cost/page")
    public Result<Page<BusItemCost>> costPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam Long familyId,
                                              @RequestParam(required = false) Long addressId,
                                              @RequestParam(required = false) Long itemId,
                                              @RequestParam(required = false) String costDateStart,
                                              @RequestParam(required = false) String costDateEnd) {
        return Result.success(itemService.pageItemCost(pageNum, pageSize, familyId, addressId, itemId, costDateStart, costDateEnd));
    }

    @Operation(summary = "新增物件费用")
    @PostMapping("/cost")
    public Result<BusItemCost> addCost(@RequestBody BusItemCost cost) {
        itemService.addItemCost(cost);
        return Result.success(cost);
    }

    @Operation(summary = "修改物件费用")
    @PutMapping("/cost")
    public Result<Void> updateCost(@RequestBody BusItemCost cost) {
        itemService.updateItemCost(cost);
        return Result.success();
    }

    @Operation(summary = "删除物件费用")
    @DeleteMapping("/cost/{id}")
    public Result<Void> deleteCost(@PathVariable Long id) {
        itemService.deleteItemCost(id);
        return Result.success();
    }

    @Operation(summary = "获取物件费用附件")
    @GetMapping("/cost/{costId}/attachments")
    public Result<List<BusItemCostAttachment>> getCostAttachments(@PathVariable Long costId) {
        return Result.success(itemService.getCostAttachments(costId));
    }

    @Operation(summary = "添加物件费用附件")
    @PostMapping("/cost/{costId}/attachment")
    public Result<Void> addCostAttachment(@PathVariable Long costId, @RequestBody BusItemCostAttachment attachment) {
        attachment.setCostId(costId);
        attachment.setId(null);
        itemService.addCostAttachment(attachment);
        return Result.success();
    }

    @Operation(summary = "删除物件费用附件")
    @DeleteMapping("/cost/attachment/{id}")
    public Result<Void> deleteCostAttachment(@PathVariable Long id) {
        itemService.deleteCostAttachment(id);
        return Result.success();
    }
}
