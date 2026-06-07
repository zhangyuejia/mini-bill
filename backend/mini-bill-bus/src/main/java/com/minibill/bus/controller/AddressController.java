package com.minibill.bus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.result.Result;
import com.minibill.bus.entity.BusAddress;
import com.minibill.bus.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 住址管理控制器
 */
@Tag(name = "住址管理")
@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "获取家庭下的所有住址")
    @GetMapping("/list/{familyId}")
    public Result<List<BusAddress>> listByFamily(@PathVariable Long familyId) {
        return Result.success(addressService.getAddressesByFamily(familyId));
    }

    @Operation(summary = "分页查询住址")
    @GetMapping("/page")
    public Result<Page<BusAddress>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam Long familyId) {
        return Result.success(addressService.pageAddress(pageNum, pageSize, familyId));
    }

    @Operation(summary = "获取住址详情")
    @GetMapping("/{id}")
    public Result<BusAddress> getById(@PathVariable Long id) {
        return Result.success(addressService.getAddressById(id));
    }

    @Operation(summary = "新增住址")
    @PostMapping
    public Result<Void> add(@RequestBody BusAddress address) {
        addressService.addAddress(address);
        return Result.success();
    }

    @Operation(summary = "修改住址")
    @PutMapping
    public Result<Void> update(@RequestBody BusAddress address) {
        addressService.updateAddress(address);
        return Result.success();
    }

    @Operation(summary = "删除住址")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return Result.success();
    }
}
