package com.middleware.edge.selfinfo.controller;

import com.middleware.edge.selfinfo.EdgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Edge 캐시 관리를 위한 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/edge/cache")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "edge.self-info.enabled", havingValue = "true", matchIfMissing = false)
public class EdgeCacheController {

    private final EdgeService edgeService;

    /**
     * Edge 캐시를 수동으로 갱신하는 API
     * DB에서 최신 데이터를 다시 가져와 캐시를 업데이트합니다.
     * 
     * @return 갱신 결과 메시지
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshCache() {
        try {
            log.info("Manual cache refresh requested");
            long startTime = System.currentTimeMillis();
            
            edgeService.initializeCaches();
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            log.info("Cache refresh completed successfully in {}ms", duration);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Edge cache refreshed successfully",
                "duration_ms", duration,
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Failed to refresh edge cache", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Failed to refresh cache: " + e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
}
