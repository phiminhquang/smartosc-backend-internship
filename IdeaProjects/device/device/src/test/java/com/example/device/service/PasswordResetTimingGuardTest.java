package com.example.device.service;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordResetTimingGuardTest {

    @Test
    void waitsUntilConfiguredMinimumHasElapsed() {
        PasswordResetTimingGuard guard = new PasswordResetTimingGuard(Duration.ofMillis(15), Duration.ZERO);

        long started = guard.start();
        guard.awaitNeutralResponseWindow(started);
        long elapsedNanos = System.nanoTime() - started;

        assertTrue(elapsedNanos >= Duration.ofMillis(15).toNanos());
    }

    @Test
    void acceptsZeroDurationWithoutDelay() {
        PasswordResetTimingGuard guard = new PasswordResetTimingGuard(Duration.ZERO, Duration.ZERO);
        assertDoesNotThrow(() -> guard.awaitNeutralResponseWindow(guard.start()));
    }

    @Test
    void rejectsNegativeOrExcessiveDurations() {
        assertThrows(IllegalArgumentException.class,
                () -> new PasswordResetTimingGuard(Duration.ofMillis(-1), Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> new PasswordResetTimingGuard(Duration.ZERO, Duration.ofSeconds(6)));
    }
}
