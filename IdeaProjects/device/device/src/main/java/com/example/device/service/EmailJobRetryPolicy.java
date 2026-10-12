package com.example.device.service;

import com.example.device.enums.EmailJobType;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class EmailJobRetryPolicy {

    private static final List<Duration> NORMAL_DELAYS = List.of(
            Duration.ofMinutes(1),
            Duration.ofMinutes(5),
            Duration.ofMinutes(30),
            Duration.ofHours(2)
    );
    private static final List<Duration> PASSWORD_RESET_DELAYS = List.of(
            Duration.ofSeconds(30),
            Duration.ofMinutes(2),
            Duration.ofMinutes(5)
    );

    public Instant nextAttemptAt(
            UUID jobId,
            EmailJobType jobType,
            int completedAttempt,
            Instant now
    ) {
        List<Duration> delays = jobType == EmailJobType.PASSWORD_RESET
                ? PASSWORD_RESET_DELAYS
                : NORMAL_DELAYS;
        int delayIndex = Math.min(Math.max(completedAttempt - 1, 0), delays.size() - 1);
        Duration baseDelay = delays.get(delayIndex);
        long jitterBoundMillis = Math.max(1, baseDelay.toMillis() / 10);
        long jitterMillis = Math.floorMod(jobId.getLeastSignificantBits(), jitterBoundMillis + 1);
        return now.plus(baseDelay).plusMillis(jitterMillis);
    }
}
