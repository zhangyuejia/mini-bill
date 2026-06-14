package com.minibill.bus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.result.Result;
import com.minibill.bus.dto.EducationSaveDTO;
import com.minibill.bus.entity.BusAttachment;
import com.minibill.bus.entity.BusEducation;
import com.minibill.bus.entity.BusEducationItem;
import com.minibill.bus.service.EducationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.minibill.common.constant.Constants.*;

@Tag(name = "教育费用")
@RestController
@RequestMapping("/education")
@RequiredArgsConstructor
public class EducationController {

    private final EducationService educationService;

    @Operation(summary = "分页查询教育费用")
    @GetMapping("/page")
    public Result<Page<BusEducation>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam Long familyId,
                                            @RequestParam(required = false) Long memberId,
                                            @RequestParam(required = false) String semesterDateStart,
                                            @RequestParam(required = false) String semesterDateEnd) {
        return Result.success(educationService.page(pageNum, pageSize, familyId, memberId, semesterDateStart, semesterDateEnd));
    }

    @Operation(summary = "获取教育费用详情")
    @GetMapping("/{id}")
    public Result<BusEducation> getById(@PathVariable Long id) {
        return Result.success(educationService.getById(id));
    }

    @Operation(summary = "新增教育费用")
    @PostMapping
    public Result<BusEducation> add(@RequestBody EducationSaveDTO dto) {
        educationService.add(dto.getEducation(), dto.getItems());
        return Result.success(dto.getEducation());
    }

    @Operation(summary = "修改教育费用")
    @PutMapping
    public Result<Void> update(@RequestBody EducationSaveDTO dto) {
        educationService.update(dto.getEducation(), dto.getItems());
        return Result.success();
    }

    @Operation(summary = "删除教育费用")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        educationService.delete(id);
        return Result.success();
    }

    // === 主表附件 ===

    @Operation(summary = "获取教育费用附件")
    @GetMapping("/{id}/attachments")
    public Result<List<BusAttachment>> getAttachments(@PathVariable Long id) {
        return Result.success(educationService.getAttachments(id));
    }

    @Operation(summary = "添加教育费用附件")
    @PostMapping("/{id}/attachment")
    public Result<Void> addAttachment(@PathVariable Long id, @RequestBody BusAttachment attachment) {
        attachment.setBizType(BIZ_TYPE_EDUCATION);
        attachment.setBizId(id);
        attachment.setId(null);
        educationService.addAttachment(attachment);
        return Result.success();
    }

    @Operation(summary = "删除教育费用附件")
    @DeleteMapping("/attachment/{id}")
    public Result<Void> deleteAttachment(@PathVariable Long id) {
        educationService.deleteAttachment(id);
        return Result.success();
    }

    // === 明细 CRUD ===

    @Operation(summary = "获取明细列表")
    @GetMapping("/{educationId}/items")
    public Result<List<BusEducationItem>> getItems(@PathVariable Long educationId) {
        return Result.success(educationService.getItems(educationId));
    }

    @Operation(summary = "新增明细")
    @PostMapping("/{educationId}/item")
    public Result<BusEducationItem> addItem(@PathVariable Long educationId, @RequestBody BusEducationItem item) {
        item.setEducationId(educationId);
        item.setId(null);
        return Result.success(educationService.addItem(item));
    }

    @Operation(summary = "修改明细")
    @PutMapping("/item/{itemId}")
    public Result<Void> updateItem(@PathVariable Long itemId, @RequestBody BusEducationItem item) {
        item.setId(itemId);
        educationService.updateItem(item);
        return Result.success();
    }

    @Operation(summary = "删除明细")
    @DeleteMapping("/item/{itemId}")
    public Result<Void> deleteItem(@PathVariable Long itemId) {
        educationService.deleteItem(itemId);
        return Result.success();
    }

    // === 明细附件 ===

    @Operation(summary = "获取明细附件")
    @GetMapping("/item/{itemId}/attachments")
    public Result<List<BusAttachment>> getItemAttachments(@PathVariable Long itemId) {
        return Result.success(educationService.getItemAttachments(itemId));
    }

    @Operation(summary = "添加明细附件")
    @PostMapping("/item/{itemId}/attachment")
    public Result<Void> addItemAttachment(@PathVariable Long itemId, @RequestBody BusAttachment attachment) {
        attachment.setBizType(BIZ_TYPE_EDUCATION_ITEM);
        attachment.setBizId(itemId);
        attachment.setId(null);
        educationService.addItemAttachment(attachment);
        return Result.success();
    }

    @Operation(summary = "删除明细附件")
    @DeleteMapping("/item/attachment/{id}")
    public Result<Void> deleteItemAttachment(@PathVariable Long id) {
        educationService.deleteItemAttachment(id);
        return Result.success();
    }
}
