package com.minibill.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minibill.bus.entity.*;
import com.minibill.bus.mapper.*;
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
    private final BusAddressMapper addressMapper;
    private final BusMaintenanceMapper maintenanceMapper;

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
            row.put("daysOwnedText", formatDaysOwned(daysOwned));
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

    @Override
    public Map<String, Object> getSummary(Long familyId) {
        Map<String, Object> summary = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        int thisYear = today.getYear();

        // === 家庭储蓄 ===
        List<BusFamilySaving> savings = savingMapper.selectList(new LambdaQueryWrapper<BusFamilySaving>()
                .eq(BusFamilySaving::getFamilyId, familyId)
                .eq(BusFamilySaving::getDelFlag, DEL_FLAG_NORMAL)
                .orderByDesc(BusFamilySaving::getSavingDate));
        BigDecimal savingTotal = BigDecimal.ZERO;
        if (!savings.isEmpty()) {
            savingTotal = savings.get(0).getTotalAmount() != null ? savings.get(0).getTotalAmount() : BigDecimal.ZERO;
        }
        summary.put("savingTotal", savingTotal);
        if (!savings.isEmpty()) {
            LocalDate lastDate = savings.get(0).getSavingDate();
            summary.put("savingLastDate", lastDate != null ? lastDate.toString() : null);
            if (lastDate != null) {
                long daysAgo = ChronoUnit.DAYS.between(lastDate, today);
                summary.put("savingDaysAgo", daysAgo);
                summary.put("savingDaysAgoText", formatDaysOwned(daysAgo));
            }
        }

        // === 房租水电 ===
        List<BusBill> bills = billMapper.selectList(new LambdaQueryWrapper<BusBill>()
                .eq(BusBill::getFamilyId, familyId)
                .eq(BusBill::getDelFlag, DEL_FLAG_NORMAL));
        BigDecimal billTotalHistory = bills.stream()
                .map(b -> b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("billTotalHistory", billTotalHistory);
        summary.put("billCount", bills.size());
        BigDecimal billTotalThisYear = bills.stream()
                .filter(b -> b.getPeriod() != null && b.getPeriod() / 100 == thisYear)
                .map(b -> b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("billTotalThisYear", billTotalThisYear);

        // === 维护费用 ===（通过住址关联家庭）
        List<BusAddress> addresses = addressMapper.selectList(new LambdaQueryWrapper<BusAddress>()
                .eq(BusAddress::getFamilyId, familyId)
                .eq(BusAddress::getDelFlag, DEL_FLAG_NORMAL));
        List<Long> addressIds = addresses.stream().map(BusAddress::getId).collect(Collectors.toList());
        List<BusMaintenance> maintenances = addressIds.isEmpty() ? List.of() :
                maintenanceMapper.selectList(new LambdaQueryWrapper<BusMaintenance>()
                        .in(BusMaintenance::getAddressId, addressIds)
                        .eq(BusMaintenance::getDelFlag, DEL_FLAG_NORMAL));
        BigDecimal maintenanceTotalHistory = maintenances.stream()
                .map(m -> m.getCost() != null ? m.getCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("maintenanceTotalHistory", maintenanceTotalHistory);
        BigDecimal maintenanceTotalThisYear = maintenances.stream()
                .filter(m -> m.getCostDate() != null && m.getCostDate().getYear() == thisYear)
                .map(m -> m.getCost() != null ? m.getCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("maintenanceTotalThisYear", maintenanceTotalThisYear);

        // === 家庭总支出（房租+维护）===
        BigDecimal totalHistory = billTotalHistory.add(maintenanceTotalHistory);
        summary.put("totalHistory", totalHistory);
        BigDecimal totalThisYear = billTotalThisYear.add(maintenanceTotalThisYear);
        summary.put("totalThisYear", totalThisYear);

        // 年均维护费用 = 总计 / 年数
        Optional<LocalDate> earliestBillDate = bills.stream()
                .filter(b -> b.getPeriod() != null)
                .map(b -> LocalDate.of(b.getPeriod() / 100, b.getPeriod() % 100, 1))
                .min(LocalDate::compareTo);
        Optional<LocalDate> earliestMaintenanceDate = maintenances.stream()
                .filter(m -> m.getCostDate() != null)
                .map(BusMaintenance::getCostDate)
                .min(LocalDate::compareTo);
        LocalDate earliestDate = null;
        if (earliestBillDate.isPresent()) earliestDate = earliestBillDate.get();
        if (earliestMaintenanceDate.isPresent() && (earliestDate == null || earliestMaintenanceDate.get().isBefore(earliestDate))) {
            earliestDate = earliestMaintenanceDate.get();
        }
        if (earliestDate != null) {
            long totalDays = ChronoUnit.DAYS.between(earliestDate, today);
            if (totalDays <= 0) totalDays = 1;
            BigDecimal years = BigDecimal.valueOf(totalDays).divide(BigDecimal.valueOf(365), 4, RoundingMode.HALF_UP);
            BigDecimal annualAvg = years.compareTo(BigDecimal.ZERO) > 0
                    ? totalHistory.divide(years, 0, RoundingMode.HALF_UP) : totalHistory;
            summary.put("annualAverage", annualAvg);
        }

        // === 物件 ===
        List<BusItem> items = itemMapper.selectList(new LambdaQueryWrapper<BusItem>()
                .eq(BusItem::getFamilyId, familyId)
                .eq(BusItem::getDelFlag, DEL_FLAG_NORMAL));
        summary.put("itemCount", items.size());

        // 物件费用汇总
        List<Long> itemIds = items.stream().map(BusItem::getId).collect(Collectors.toList());
        List<BusItemCost> allCosts = itemIds.isEmpty() ? List.of() :
                itemCostMapper.selectList(new LambdaQueryWrapper<BusItemCost>()
                        .in(BusItemCost::getItemId, itemIds)
                        .eq(BusItemCost::getDelFlag, DEL_FLAG_NORMAL));
        Map<Long, BigDecimal> costSumMap = allCosts.stream()
                .collect(Collectors.groupingBy(BusItemCost::getItemId,
                        Collectors.mapping(BusItemCost::getCost,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        // 物件费用今年合计
        BigDecimal itemCostThisYear = allCosts.stream()
                .filter(c -> c.getCostDate() != null && c.getCostDate().getYear() == thisYear)
                .map(c -> c.getCost() != null ? c.getCost() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.put("itemCostThisYear", itemCostThisYear);

        // 今年总支出 = 房租水电 + 维护 + 物件费用
        summary.put("totalExpenseThisYear", totalThisYear.add(itemCostThisYear));

        // 最贵/最便宜物件（日均）
        Map<String, Object> mostExpensive = null;
        Map<String, Object> cheapest = null;
        for (BusItem item : items) {
            if (item.getPurchaseDate() == null || item.getPurchaseAmount() == null) continue;
            LocalDate endDate = item.getDeactivationDate() != null ? item.getDeactivationDate() : today;
            long days = ChronoUnit.DAYS.between(item.getPurchaseDate(), endDate);
            if (days <= 0) days = 1;
            BigDecimal mc = costSumMap.getOrDefault(item.getId(), BigDecimal.ZERO);
            BigDecimal rv = item.getResidualValue() != null ? item.getResidualValue() : BigDecimal.ZERO;
            BigDecimal totalCost = item.getPurchaseAmount().add(mc).subtract(rv);
            BigDecimal daily = totalCost.divide(BigDecimal.valueOf(days), 2, RoundingMode.HALF_UP);

            if (mostExpensive == null || daily.compareTo((BigDecimal) mostExpensive.get("dailyCost")) > 0) {
                mostExpensive = Map.of("name", item.getName(), "dailyCost", daily,
                        "totalCost", totalCost, "daysOwned", days,
                        "daysOwnedText", formatDaysOwned(days));
            }
            if (cheapest == null || daily.compareTo((BigDecimal) cheapest.get("dailyCost")) < 0) {
                cheapest = Map.of("name", item.getName(), "dailyCost", daily,
                        "totalCost", totalCost, "daysOwned", days,
                        "daysOwnedText", formatDaysOwned(days));
            }
        }
        if (mostExpensive != null) summary.put("mostExpensiveItem", mostExpensive);
        if (cheapest != null) summary.put("cheapestItem", cheapest);

        return summary;
    }

    private String formatDaysOwned(long days) {
        long years = days / 365;
        long months = (days % 365) / 30;
        long remainDays = days % 365 % 30;
        StringBuilder sb = new StringBuilder();
        if (years > 0) sb.append(years).append("年");
        if (months > 0) sb.append(months).append("月");
        if (remainDays > 0) sb.append(remainDays).append("天");
        if (sb.length() == 0) sb.append("0天");
        return days + "天（" + sb + "）";
    }
}
