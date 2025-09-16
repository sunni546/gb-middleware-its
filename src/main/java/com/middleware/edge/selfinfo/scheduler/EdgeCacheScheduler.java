package com.middleware.edge.selfinfo.scheduler;

import com.middleware.edge.selfinfo.EdgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Edge 캐시 자동 갱신을 위한 스케줄러
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "edge.cache.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class EdgeCacheScheduler {

    private final EdgeService edgeService;

    /**
     * 주기적으로 Edge 캐시 갱신
     */
    @Scheduled(cron = "${edge.cache.scheduler.cron:0 0 0 * * *}")
    public void refreshEdgeCache() {
        try {
            log.info("Starting scheduled edge cache refresh");
            long startTime = System.currentTimeMillis();
            
            edgeService.initializeCaches();
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            log.info("Scheduled edge cache refresh completed successfully in {}ms", duration);
            
        } catch (Exception e) {
            log.error("Failed to refresh edge cache during scheduled execution", e);
        }
    }
}
