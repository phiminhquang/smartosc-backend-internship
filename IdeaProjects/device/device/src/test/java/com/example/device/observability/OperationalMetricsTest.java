package com.example.device.observability;

import com.example.device.enums.EmailJobStatus;
import com.example.device.enums.EmailJobType;
import com.example.device.repository.EmailJobRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OperationalMetricsTest {

    @Test
    void exposesBoundedEmailAndSchedulerMetricsWithoutIdentifiers() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        EmailJobRepository repository = mock(EmailJobRepository.class);
        when(repository.countByStatus(EmailJobStatus.PENDING)).thenReturn(3L);
        OperationalMetrics metrics = new OperationalMetrics(registry, repository);
        metrics.bindEmailJobGauges();

        metrics.recordSchedulerRun("assignment_notifications", () -> {
        });
        assertThrows(IllegalStateException.class,
                () -> metrics.recordSchedulerRun("email_poll", () -> {
                    throw new IllegalStateException("sensitive-message");
                }));
        metrics.recordEmailBatchSize(2);
        metrics.recordEmailDelivery(EmailJobType.PASSWORD_RESET, "sent");
        metrics.recordEmailDeliverySkipped();
        metrics.recordRecoveredLeases(2);
        metrics.recordCleanedJobs(4);

        assertEquals(3.0, registry.get("device.email.jobs")
                .tag("status", "pending").gauge().value());
        assertEquals(1L, registry.get("device.scheduler.duration")
                .tags("scheduler", "assignment_notifications", "outcome", "success")
                .timer().count());
        assertEquals(1L, registry.get("device.scheduler.duration")
                .tags("scheduler", "email_poll", "outcome", "failure")
                .timer().count());
        assertEquals(2.0, registry.get("device.email.worker.batch.size").summary().totalAmount());
        assertEquals(1.0, registry.get("device.email.worker.deliveries")
                .tags("job_type", "password_reset", "outcome", "sent")
                .counter().count());
        assertEquals(1.0, registry.get("device.email.worker.deliveries")
                .tags("job_type", "unknown", "outcome", "skipped")
                .counter().count());
        assertEquals(2.0, registry.get("device.email.worker.recovered").counter().count());
        assertEquals(4.0, registry.get("device.email.worker.cleaned").counter().count());
        assertEquals(7, registry.find("device.email.jobs").gauges().size());
    }
}
