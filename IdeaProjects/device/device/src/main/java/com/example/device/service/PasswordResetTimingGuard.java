package com.example.device.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

@Component
public class PasswordResetTimingGuard {

    private static final Duration MAX_CONFIGURED_DURATION = Duration.ofSeconds(5);

    private final long minimumNanos;
    private final long jitterNanos;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetTimingGuard(
            @Value("${app.password-reset.response-min-duration:150ms}") Duration minimumDuration,
            @Value("${app.password-reset.response-jitter:25ms}") Duration jitter) {
        this.minimumNanos = validatedNanos("response-min-duration", minimumDuration);
        this.jitterNanos = validatedNanos("response-jitter", jitter);
    }

    public long start() {
        return System.nanoTime();
    }

    public void awaitNeutralResponseWindow(long startedNanos) {
        long randomJitter = jitterNanos == 0
                ? 0
                : (long) (secureRandom.nextDouble() * (jitterNanos + 1));
        long deadline = startedNanos + minimumNanos + randomJitter;

        while (!Thread.currentThread().isInterrupted()) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                return;
            }
            LockSupport.parkNanos(remaining);
        }
    }

    private long validatedNanos(String property, Duration value) {
        if (value.isNegative() || value.compareTo(MAX_CONFIGURED_DURATION) > 0) {
            throw new IllegalArgumentException(
                    "app.password-reset." + property + " must be between 0 and 5 seconds"
            );
        }
        return value.toNanos();
    }
}
