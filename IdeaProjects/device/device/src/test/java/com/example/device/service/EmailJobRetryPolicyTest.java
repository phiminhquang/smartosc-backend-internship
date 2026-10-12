package com.example.device.service;

import com.example.device.enums.EmailJobType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailJobRetryPolicyTest {

    private final EmailJobRetryPolicy policy = new EmailJobRetryPolicy();
    private final Instant now = Instant.parse("2026-10-10T00:00:00Z");
    private final UUID jobId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void normalJobsUseApprovedBackoffWithBoundedJitter() {
        assertDelayBetween(1, EmailJobType.ASSIGNMENT_REMINDER, 60, 66);
        assertDelayBetween(2, EmailJobType.ASSIGNMENT_REMINDER, 300, 330);
        assertDelayBetween(3, EmailJobType.ASSIGNMENT_REMINDER, 1800, 1980);
        assertDelayBetween(4, EmailJobType.ASSIGNMENT_REMINDER, 7200, 7920);
    }

    @Test
    void resetJobsUseShortApprovedBackoffWithBoundedJitter() {
        assertDelayBetween(1, EmailJobType.PASSWORD_RESET, 30, 33);
        assertDelayBetween(2, EmailJobType.PASSWORD_RESET, 120, 132);
        assertDelayBetween(3, EmailJobType.PASSWORD_RESET, 300, 330);
    }

    private void assertDelayBetween(
            int attempt,
            EmailJobType type,
            long minimumSeconds,
            long maximumSeconds
    ) {
        Instant next = policy.nextAttemptAt(jobId, type, attempt, now);
        assertTrue(next.compareTo(now.plusSeconds(minimumSeconds)) >= 0);
        assertTrue(next.compareTo(now.plusSeconds(maximumSeconds)) <= 0);
    }
}
