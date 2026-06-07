package com.minibill.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.result.Result;
import com.minibill.system.entity.SysDictData;
import com.minibill.system.entity.SysDictType;
import com.minibill.system.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理控制器
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    // ===== 字典类型 =====

    @Operation(summary = "分页查询字典类型")
    @GetMapping("/type/page")
    public Result<Page<SysDictType>> pageType(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) String keyword) {
        return Result.success(dictService.pageDictType(pageNum, pageSize, keyword));
    }

    @Operation(summary = "查询所有字典类型")
    @GetMapping("/type/list")
    public Result<List<SysDictType>> listType() {
        return Result.success(dictService.listDictType());
    }

    @Operation(summary = "新增字典类型")
    @PostMapping("/type")
    public Result<Void> addType(@RequestBody SysDictType dictType) {
        dictService.addDictType(dictType);
        return Result.success();
    }

    @Operation(summary = "修改字典类型")
    @PutMapping("/type")
    public Result<Void> updateType(@RequestBody SysDictType dictType) {
        dictService.updateDictType(dictType);
        return Result.success();
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/type/{id}")
    public Result<Void> deleteType(@PathVariable Long id) {
        dictService.deleteDictType(id);
        return Result.success();
    }

    // ===== 字典数据 =====

    @Operation(summary = "分页查询字典数据")
    @GetMapping("/data/page")
    public Result<Page<SysDictData>> pageData(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam Long dictTypeId) {
        return Result.success(dictService.pageDictData(pageNum, pageSize, dictTypeId));
    }

    @Operation(summary = "根据字典编码获取字典数据")
    @GetMapping("/data/code/{dictCode}")
    public Result<List<SysDictData>> getDataByCode(@PathVariable String dictCode) {
        return Result.success(dictService.getDictDataByCode(dictCode));
    }

    @Operation(summary = "新增字典数据")
    @PostMapping("/data")
    public Result<Void> addData(@RequestBody SysDictData dictData) {
        dictService.addDictData(dictData);
        return Result.success();
    }

    @Operation(summary = "修改字典数据")
    @PutMapping("/data")
    public Result<Void> updateData(@RequestBody SysDictData dictData) {
        dictService.updateDictData(dictData);
        return Result.success();
    }

    @Operation(summary = "删除字典数据")
    @DeleteMapping("/data/{id}")
    public Result<Void> deleteData(@PathVariable Long id) {
        dictService.deleteDictData(id);
        return Result.success();
    }
}
