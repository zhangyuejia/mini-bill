package com.minibill.bus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.result.Result;
import com.minibill.bus.entity.BusBill;
import com.minibill.bus.entity.BusBillAttachment;
import com.minibill.bus.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账单管理控制器
 */
@Tag(name = "账单管理")
@RestController
@RequestMapping("/bill")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @Operation(summary = "分页查询账单")
    @GetMapping("/page")
    public Result<Page<BusBill>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                      @RequestParam Long familyId,
                                      @RequestParam(required = false) Long addressId,
                                      @RequestParam(required = false) Integer periodStart,
                                      @RequestParam(required = false) Integer periodEnd) {
        return Result.success(billService.pageBill(pageNum, pageSize, familyId, addressId, periodStart, periodEnd));
    }

    @Operation(summary = "获取账单详情")
    @GetMapping("/{id}")
    public Result<BusBill> getById(@PathVariable Long id) {
        return Result.success(billService.getBillById(id));
    }

    @Operation(summary = "新增账单")
    @PostMapping
    public Result<BusBill> add(@RequestBody BusBill bill) {
        billService.addBill(bill);
        return Result.success(bill);
    }

    @Operation(summary = "修改账单")
    @PutMapping
    public Result<Void> update(@RequestBody BusBill bill) {
        billService.updateBill(bill);
        return Result.success();
    }

    @Operation(summary = "删除账单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        billService.deleteBill(id);
        return Result.success();
    }

    @Operation(summary = "获取账单附件")
    @GetMapping("/{billId}/attachments")
    public Result<List<BusBillAttachment>> getAttachments(@PathVariable Long billId) {
        return Result.success(billService.getAttachments(billId));
    }

    @Operation(summary = "添加账单附件")
    @PostMapping("/{billId}/attachment")
    public Result<Void> addAttachment(@PathVariable Long billId, @RequestBody BusBillAttachment attachment) {
        attachment.setBillId(billId);
        attachment.setId(null);
        billService.addAttachment(attachment);
        return Result.success();
    }

    @Operation(summary = "删除账单附件")
    @DeleteMapping("/attachment/{id}")
    public Result<Void> deleteAttachment(@PathVariable Long id) {
        billService.deleteAttachment(id);
        return Result.success();
    }

    @Operation(summary = "迁移历史账单：从备注提取管理费")
    @PostMapping("/migrate-management-fee")
    public Result<String> migrateManagementFee() {
        int count = billService.migrateManagementFee();
        return Result.success("迁移完成，共处理 " + count + " 条记录");
    }
}
