package com.minibill.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minibill.bus.entity.BusBill;
import com.minibill.bus.entity.BusFamilySaving;
import com.minibill.bus.mapper.BusBillMapper;
import com.minibill.bus.mapper.BusFamilySavingMapper;
import com.minibill.bus.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.minibill.common.constant.Constants.DEL_FLAG_NORMAL;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BusFamilySavingMapper savingMapper;
    private final BusBillMapper billMapper;

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
}
