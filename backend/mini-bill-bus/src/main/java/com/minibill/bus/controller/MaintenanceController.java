package com.minibill.bus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.result.Result;
import com.minibill.bus.entity.BusAttachment;
import com.minibill.bus.entity.BusMaintenance;
import com.minibill.bus.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.minibill.common.constant.Constants.*;

@Tag(name = "维护费用")
@RestController
@RequestMapping("/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @Operation(summary = "分页查询维护费用")
    @GetMapping("/page")
    public Result<Page<BusMaintenance>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam Long familyId,
                                              @RequestParam(required = false) Long addressId,
                                              @RequestParam(required = false) String type,
                                              @RequestParam(required = false) String costDateStart,
                                              @RequestParam(required = false) String costDateEnd) {
        return Result.success(maintenanceService.page(pageNum, pageSize, familyId, addressId, type, costDateStart, costDateEnd));
    }

    @Operation(summary = "获取维护费用详情")
    @GetMapping("/{id}")
    public Result<BusMaintenance> getById(@PathVariable Long id) {
        return Result.success(maintenanceService.getById(id));
    }

    @Operation(summary = "新增维护费用")
    @PostMapping
    public Result<BusMaintenance> add(@RequestBody BusMaintenance maintenance) {
        maintenanceService.add(maintenance);
        return Result.success(maintenance);
    }

    @Operation(summary = "修改维护费用")
    @PutMapping
    public Result<Void> update(@RequestBody BusMaintenance maintenance) {
        maintenanceService.update(maintenance);
        return Result.success();
    }

    @Operation(summary = "删除维护费用")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        maintenanceService.delete(id);
        return Result.success();
    }

    @Operation(summary = "获取维护费用附件")
    @GetMapping("/{id}/attachments")
    public Result<List<BusAttachment>> getAttachments(@PathVariable Long id) {
        return Result.success(maintenanceService.getAttachments(id));
    }

    @Operation(summary = "添加维护费用附件")
    @PostMapping("/{id}/attachment")
    public Result<Void> addAttachment(@PathVariable Long id, @RequestBody BusAttachment attachment) {
        attachment.setBizType(BIZ_TYPE_MAINTENANCE);
        attachment.setBizId(id);
        attachment.setId(null);
        maintenanceService.addAttachment(attachment);
        return Result.success();
    }

    @Operation(summary = "删除维护费用附件")
    @DeleteMapping("/attachment/{id}")
    public Result<Void> deleteAttachment(@PathVariable Long id) {
        maintenanceService.deleteAttachment(id);
        return Result.success();
    }
}
