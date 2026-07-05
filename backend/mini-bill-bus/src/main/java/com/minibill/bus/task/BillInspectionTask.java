package com.minibill.bus.task;

import com.minibill.bus.service.BillInspectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 账单巡检定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "minibill.inspection", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BillInspectionTask {

    private final BillInspectionService billInspectionService;

    @Scheduled(fixedDelayString = "${minibill.inspection.interval:1800000}", initialDelay = 30000)
    public void runInspection() {
        log.info("账单巡检定时任务开始执行");
        try {
            billInspectionService.inspectBills();
            log.info("账单巡检定时任务执行完成");
        } catch (Exception e) {
            log.error("账单巡检定时任务执行异常", e);
        }
    }
}
