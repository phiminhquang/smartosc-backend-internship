package com.example.device.scheduler;

import com.example.device.configuration.RateLimitProperties;
import com.example.device.observability.OperationalMetrics;
import com.example.device.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class RateLimitCleanupScheduler {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties properties;
    private final Clock clock;
    private final OperationalMetrics metrics;

    @Scheduled(
            cron = "${app.rate-limit.cleanup-cron:0 15 3 * * *}",
            zone = "${app.scheduler.zone:Asia/Ho_Chi_Minh}"
    )
    public void cleanup() {
        metrics.recordSchedulerRun("rate_limit_cleanup", () -> {
            int deleted = rateLimitService.cleanupExpired(clock.instant(), properties.getCleanupBatchSize());
            if (deleted > 0) {
                log.info("event=rate_limit_cleanup deleted_count={}", deleted);
            }
        });
    }
}
