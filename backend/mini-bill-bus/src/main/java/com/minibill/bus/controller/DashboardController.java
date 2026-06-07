package com.minibill.bus.controller;

import com.minibill.common.result.Result;
import com.minibill.bus.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 首页仪表盘数据
 */
@Tag(name = "仪表盘")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取储蓄趋势数据")
    @GetMapping("/saving-trend")
    public Result<List<Map<String, Object>>> savingTrend(@RequestParam Long familyId) {
        return Result.success(dashboardService.getSavingTrend(familyId));
    }

    @Operation(summary = "获取房租水电同比数据（按月）")
    @GetMapping("/bill-compare")
    public Result<List<Map<String, Object>>> billCompare(@RequestParam Long familyId,
                                                         @RequestParam(required = false) Long addressId) {
        return Result.success(dashboardService.getBillCompare(familyId, addressId));
    }

    @Operation(summary = "获取物件日均成本")
    @GetMapping("/item-daily-cost")
    public Result<List<Map<String, Object>>> itemDailyCost(@RequestParam Long familyId) {
        return Result.success(dashboardService.getItemDailyCost(familyId));
    }
}
