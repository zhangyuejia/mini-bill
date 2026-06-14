package com.minibill.bus.service;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    List<Map<String, Object>> getSavingTrend(Long familyId);

    List<Map<String, Object>> getBillCompare(Long familyId, Long addressId);

    List<Map<String, Object>> getItemDailyCost(Long familyId);

    Map<String, Object> getSummary(Long familyId);
}
