package com.example.device.observability;

import com.example.device.enums.EmailJobStatus;
import com.example.device.enums.EmailJobType;
import com.example.device.repository.EmailJobRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class OperationalMetrics {

    private static final String UNKNOWN_JOB_TYPE = "unknown";

    private final MeterRegistry meterRegistry;
    private final EmailJobRepository emailJobRepository;

    public OperationalMetrics(MeterRegistry meterRegistry, EmailJobRepository emailJobRepository) {
        this.meterRegistry = meterRegistry;
        this.emailJobRepository = emailJobRepository;
    }

    @PostConstruct
    void bindEmailJobGauges() {
        for (EmailJobStatus status : EmailJobStatus.values()) {
            Gauge.builder(
                            "device.email.jobs",
                            emailJobRepository,
                            repository -> repository.countByStatus(status)
                    )
                    .description("Current durable email jobs by lifecycle status")
                    .tag("status", metricValue(status))
                    .register(meterRegistry);
        }
    }

    public void recordSchedulerRun(String scheduler, Runnable action) {
        Timer.Sample sample = Timer.start(meterRegistry);
        boolean succeeded = false;
        try {
            action.run();
            succeeded = true;
        } finally {
            sample.stop(Timer.builder("device.scheduler.duration")
                    .description("Scheduled task execution duration")
                    .tag("scheduler", scheduler)
                    .tag("outcome", succeeded ? "success" : "failure")
                    .register(meterRegistry));
        }
    }

    public void recordEmailBatchSize(int claimedJobs) {
        DistributionSummary.builder("device.email.worker.batch.size")
                .description("Number of email jobs claimed in a worker poll")
                .register(meterRegistry)
                .record(claimedJobs);
    }

    public void recordEmailDelivery(EmailJobType jobType, String outcome) {
        recordEmailDelivery(jobType == null ? UNKNOWN_JOB_TYPE : metricValue(jobType), outcome);
    }

    public void recordEmailDeliverySkipped() {
        recordEmailDelivery(UNKNOWN_JOB_TYPE, "skipped");
    }

    public void recordRecoveredLeases(int recovered) {
        recordCount("device.email.worker.recovered", "Recovered expired email-job leases", recovered);
    }

    public void recordCleanedJobs(int deleted) {
        recordCount("device.email.worker.cleaned", "Email jobs removed by retention cleanup", deleted);
    }

    private void recordEmailDelivery(String jobType, String outcome) {
        Counter.builder("device.email.worker.deliveries")
                .description("Email delivery attempts by type and bounded outcome")
                .tag("job_type", jobType)
                .tag("outcome", outcome)
                .register(meterRegistry)
                .increment();
    }

    private void recordCount(String name, String description, int amount) {
        if (amount <= 0) {
            return;
        }
        Counter.builder(name)
                .description(description)
                .register(meterRegistry)
                .increment(amount);
    }

    private String metricValue(Enum<?> value) {
        return value.name().toLowerCase(Locale.ROOT);
    }
}
