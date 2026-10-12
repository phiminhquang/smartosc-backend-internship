package com.example.device.scheduler;

import com.example.device.configuration.EmailJobProperties;
import com.example.device.enums.EmailJobType;
import com.example.device.observability.OperationalMetrics;
import com.example.device.service.EmailJobClaimService;
import com.example.device.service.EmailJobDelivery;
import com.example.device.service.EmailJobSender;
import com.example.device.service.EmailJobStateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(OutputCaptureExtension.class)
class EmailJobWorkerLoggingTest {

    private static final Instant NOW = Instant.parse("2026-10-11T02:00:00Z");

    @Test
    void retryableFailureLogsJobMetadataWithoutRecipientPayloadOrExceptionMessage(CapturedOutput output) {
        UUID jobId = UUID.fromString("3fc5733a-51f4-4ae4-bd43-158a4ba34bdf");
        EmailJobProperties properties = new EmailJobProperties();
        properties.setBatchSize(20);
        properties.setLeaseDuration(Duration.ofSeconds(60));
        EmailJobDelivery delivery = new EmailJobDelivery(
                jobId,
                "request-worker-123",
                EmailJobType.PASSWORD_RESET,
                "private-recipient@device.test",
                "Private password reset",
                "email/password-reset",
                1,
                "{\"password\":\"NeverLog\",\"token\":\"raw-reset-token\"}",
                new byte[]{1, 2, 3},
                new byte[12],
                "test-v1",
                UUID.randomUUID(),
                NOW.plusSeconds(900),
                2,
                4
        );
        EmailJobClaimService claimService = (leaseOwner, now, leaseDuration, batchSize) -> List.of(jobId);
        EmailJobStateService stateService = stateServiceReturning(delivery);
        EmailJobSender sender = ignored -> {
            throw new RuntimeException("SMTP password=NeverLog token=raw-reset-token");
        };
        EmailJobWorker worker = new EmailJobWorker(
                claimService,
                stateService,
                sender,
                properties,
                Clock.fixed(NOW, ZoneOffset.UTC),
                Runnable::run,
                mock(OperationalMetrics.class)
        );

        worker.processBatch();

        String logs = output.getAll();
        assertTrue(logs.contains("event=email_job_delivery_failed"));
        assertTrue(logs.contains("request_id=request-worker-123"));
        assertTrue(logs.contains("job_id=" + jobId));
        assertTrue(logs.contains("job_type=PASSWORD_RESET"));
        assertTrue(logs.contains("failure_kind=retryable"));
        assertTrue(logs.contains("exception_type=RuntimeException"));
        assertFalse(logs.contains("private-recipient@device.test"));
        assertFalse(logs.contains("NeverLog"));
        assertFalse(logs.contains("raw-reset-token"));
    }

    private EmailJobStateService stateServiceReturning(EmailJobDelivery delivery) {
        return new EmailJobStateService() {
            @Override
            public Optional<EmailJobDelivery> loadForDelivery(UUID id, String leaseOwner, Instant now) {
                return Optional.of(delivery);
            }

            @Override
            public boolean markSent(UUID id, String leaseOwner, Instant now) {
                return true;
            }

            @Override
            public boolean markFailed(
                    UUID id,
                    String leaseOwner,
                    Instant now,
                    boolean permanent,
                    String errorCode,
                    String errorSummary
            ) {
                return true;
            }

            @Override
            public int recoverExpiredLeases(Instant now, int batchSize) {
                return 0;
            }

            @Override
            public int cleanup(Instant now, int batchSize) {
                return 0;
            }
        };
    }
}
