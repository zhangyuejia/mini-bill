package com.minibill.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minibill.bus.entity.BusBill;
import com.minibill.bus.entity.BusFamilySaving;
import com.minibill.bus.entity.BusItem;
import com.minibill.bus.entity.BusItemCost;
import com.minibill.bus.mapper.BusBillMapper;
import com.minibill.bus.mapper.BusFamilySavingMapper;
import com.minibill.bus.mapper.BusItemCostMapper;
import com.minibill.bus.mapper.BusItemMapper;
import com.minibill.bus.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.minibill.common.constant.Constants.DEL_FLAG_NORMAL;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BusFamilySavingMapper savingMapper;
    private final BusBillMapper billMapper;
    private final BusItemMapper itemMapper;
    private final BusItemCostMapper itemCostMapper;

    @Override
    public List<Map<String, Object>> getSavingTrend(Long familyId) {
        List<BusFamilySaving> list = savingMapper.selectList(new LambdaQueryWrapper<BusFamilySaving>()
                .eq(BusFamilySaving::getFamilyId, familyId)
                .eq(BusFamilySaving::getDelFlag, DEL_FLAG_NORMAL)
                .orderByAsc(BusFamilySaving::getSavingDate));

        List<Map<String, Object>> result = new ArrayList<>();
        for (BusFamilySaving s : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", s.getSavingDate() != null ? s.getSavingDate().toString() : "");
            item.put("total", s.getTotalAmount());
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getBillCompare(Long familyId, Long addressId) {
        LambdaQueryWrapper<BusBill> wrapper = new LambdaQueryWrapper<BusBill>()
                .eq(BusBill::getFamilyId, familyId)
                .eq(addressId != null, BusBill::getAddressId, addressId)
                .eq(BusBill::getDelFlag, DEL_FLAG_NORMAL)
                .orderByAsc(BusBill::getPeriod);

        List<BusBill> list = billMapper.selectList(wrapper);

        // Group by year
        Map<Integer, List<BusBill>> byYear = list.stream()
                .filter(b -> b.getPeriod() != null)
                .collect(Collectors.groupingBy(b -> b.getPeriod() / 100,
                        TreeMap::new, Collectors.toList()));

        // Collect month labels from all months present (1-12)
        Set<Integer> allMonths = new TreeSet<>();
        for (List<BusBill> bills : byYear.values()) {
            for (BusBill b : bills) {
                allMonths.add(b.getPeriod() % 100);
            }
        }

        // Build result: one row per month with year columns
        List<Map<String, Object>> result = new ArrayList<>();
        for (int month : allMonths) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", month + "月");
            for (Map.Entry<Integer, List<BusBill>> entry : byYear.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Optional<BusBill> match = entry.getValue().stream()
                        .filter(b -> (b.getPeriod() % 100) == month)
                        .findFirst();
                row.put(key, match.map(BusBill::getTotalAmount).orElse(null));
            }
            result.add(row);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getItemDailyCost(Long familyId) {
        // 1. 查所有正常物件
        List<BusItem> items = itemMapper.selectList(new LambdaQueryWrapper<BusItem>()
                .eq(BusItem::getFamilyId, familyId)
                .eq(BusItem::getDelFlag, DEL_FLAG_NORMAL));

        // 2. 查所有物件费用记录（通过物件ID关联家庭）
        List<Long> itemIds = items.stream().map(BusItem::getId).collect(Collectors.toList());
        List<BusItemCost> allCosts = itemIds.isEmpty() ? List.of() : itemCostMapper.selectList(new LambdaQueryWrapper<BusItemCost>()
                .in(BusItemCost::getItemId, itemIds)
                .eq(BusItemCost::getDelFlag, DEL_FLAG_NORMAL));

        // 按 itemId 分组汇总费用
        Map<Long, BigDecimal> costSumMap = allCosts.stream()
                .collect(Collectors.groupingBy(BusItemCost::getItemId,
                        Collectors.mapping(BusItemCost::getCost,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();

        for (BusItem item : items) {
            if (item.getPurchaseDate() == null || item.getPurchaseAmount() == null) continue;

            // 有停用时间则计算到停用为止，否则到今天
            LocalDate endDate = item.getDeactivationDate() != null ? item.getDeactivationDate() : today;
            long daysOwned = ChronoUnit.DAYS.between(item.getPurchaseDate(), endDate);
            if (daysOwned <= 0) daysOwned = 1;

            BigDecimal maintenanceCost = costSumMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            BigDecimal residualValue = item.getResidualValue() != null ? item.getResidualValue() : BigDecimal.ZERO;
            // 总投入 = 购买金额 + 维护费用 - 残值
            BigDecimal totalCost = item.getPurchaseAmount().add(maintenanceCost).subtract(residualValue);

            BigDecimal dailyCost = totalCost.divide(BigDecimal.valueOf(daysOwned), 2, RoundingMode.HALF_UP);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", item.getName());
            row.put("purchaseAmount", item.getPurchaseAmount());
            row.put("maintenanceCost", maintenanceCost);
            row.put("residualValue", residualValue);
            row.put("totalCost", totalCost);
            row.put("daysOwned", daysOwned);
            row.put("dailyCost", dailyCost);
            row.put("purchaseDate", item.getPurchaseDate().toString());
            if (item.getDeactivationDate() != null) {
                row.put("deactivationDate", item.getDeactivationDate().toString());
            }
            result.add(row);
        }

        // 按日均成本降序
        result.sort((a, b) -> ((BigDecimal) b.get("dailyCost")).compareTo((BigDecimal) a.get("dailyCost")));
        return result;
    }
}
