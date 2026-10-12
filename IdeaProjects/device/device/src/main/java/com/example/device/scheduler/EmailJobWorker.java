package com.example.device.scheduler;

import com.example.device.configuration.EmailJobProperties;
import com.example.device.observability.OperationalMetrics;
import com.example.device.observability.RequestCorrelation;
import com.example.device.service.EmailJobClaimService;
import com.example.device.service.EmailJobDelivery;
import com.example.device.service.EmailJobPermanentException;
import com.example.device.service.EmailJobSender;
import com.example.device.service.EmailJobStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
@Slf4j
public class EmailJobWorker {

    private static final String TRANSIENT_ERROR_CODE = "EMAIL_SEND_FAILED";
    private static final String TRANSIENT_ERROR_SUMMARY =
            "Email delivery failed; inspect redacted application logs using the job id";
    private static final String PERMANENT_ERROR_CODE = "INVALID_EMAIL_JOB";
    private static final String PERMANENT_ERROR_SUMMARY =
            "Email job configuration or payload is invalid";

    private final EmailJobClaimService claimService;
    private final EmailJobStateService stateService;
    private final EmailJobSender sender;
    private final EmailJobProperties properties;
    private final Clock clock;
    private final Executor taskExecutor;
    private final OperationalMetrics metrics;
    private final String leaseOwner = "email-worker-" + UUID.randomUUID();

    public EmailJobWorker(
            EmailJobClaimService claimService,
            EmailJobStateService stateService,
            EmailJobSender sender,
            EmailJobProperties properties,
            Clock clock,
            @Qualifier("emailJobTaskExecutor") Executor taskExecutor,
            OperationalMetrics metrics
    ) {
        this.claimService = claimService;
        this.stateService = stateService;
        this.sender = sender;
        this.properties = properties;
        this.clock = clock;
        this.taskExecutor = taskExecutor;
        this.metrics = metrics;
    }

    @Scheduled(fixedDelayString = "${app.email-job.poll-delay-ms:5000}")
    public void poll() {
        if (properties.isWorkerEnabled()) {
            metrics.recordSchedulerRun("email_poll", this::processBatch);
        }
    }

    @Scheduled(fixedDelayString = "${app.email-job.recovery-delay-ms:30000}")
    public void recover() {
        if (properties.isWorkerEnabled()) {
            metrics.recordSchedulerRun("email_recovery", this::recoverExpiredLeases);
        }
    }

    @Scheduled(
            cron = "${app.email-job.cleanup-cron:0 0 3 * * *}",
            zone = "${app.scheduler.zone:Asia/Ho_Chi_Minh}"
    )
    public void scheduledCleanup() {
        if (properties.isWorkerEnabled()) {
            metrics.recordSchedulerRun("email_cleanup", this::cleanup);
        }
    }

    public int processBatch() {
        Instant claimTime = clock.instant();
        List<UUID> claimedIds = claimService.claimBatch(
                leaseOwner,
                claimTime,
                properties.getLeaseDuration(),
                properties.getBatchSize()
        );
        metrics.recordEmailBatchSize(claimedIds.size());
        CompletableFuture<?>[] deliveries = claimedIds.stream()
                .map(id -> CompletableFuture.runAsync(() -> deliver(id), taskExecutor))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(deliveries).join();
        return claimedIds.size();
    }

    public int recoverExpiredLeases() {
        int recovered = stateService.recoverExpiredLeases(clock.instant(), properties.getBatchSize());
        if (recovered > 0) {
            log.warn("event=email_job_lease_recovered recovered_count={} terminal_status=UNKNOWN", recovered);
        }
        metrics.recordRecoveredLeases(recovered);
        return recovered;
    }

    public int cleanup() {
        int deleted = stateService.cleanup(clock.instant(), properties.getBatchSize());
        if (deleted > 0) {
            log.info("event=email_job_cleanup deleted_count={}", deleted);
        }
        metrics.recordCleanedJobs(deleted);
        return deleted;
    }

    private void deliver(UUID id) {
        Instant loadTime = clock.instant();
        Optional<EmailJobDelivery> delivery = stateService.loadForDelivery(id, leaseOwner, loadTime);
        if (delivery.isEmpty()) {
            metrics.recordEmailDeliverySkipped();
            return;
        }

        EmailJobDelivery current = delivery.get();
        try (RequestCorrelation.Scope ignored = RequestCorrelation.open(current.requestId())) {
            try {
                sender.send(current);
                if (!stateService.markSent(id, leaseOwner, clock.instant())) {
                    metrics.recordEmailDelivery(current.jobType(), "unknown");
                    log.warn(
                            "event=email_job_delivery_unknown request_id={} job_id={} reason=lease_lost",
                            RequestCorrelation.forLog(current.requestId()),
                            id
                    );
                    return;
                }
                metrics.recordEmailDelivery(current.jobType(), "sent");
                log.info(
                        "event=email_job_delivery_succeeded request_id={} job_id={} job_type={} attempt={}",
                        RequestCorrelation.forLog(current.requestId()),
                        id,
                        current.jobType(),
                        current.attemptCount()
                );
            } catch (EmailJobPermanentException e) {
                metrics.recordEmailDelivery(current.jobType(), "permanent_failure");
                log.warn(
                        "event=email_job_delivery_failed request_id={} job_id={} job_type={} attempt={} failure_kind=permanent",
                        RequestCorrelation.forLog(current.requestId()),
                        id,
                        current.jobType(),
                        current.attemptCount()
                );
                stateService.markFailed(
                        id,
                        leaseOwner,
                        clock.instant(),
                        true,
                        PERMANENT_ERROR_CODE,
                        PERMANENT_ERROR_SUMMARY
                );
            } catch (RuntimeException e) {
                metrics.recordEmailDelivery(current.jobType(), "retryable_failure");
                log.warn(
                        "event=email_job_delivery_failed request_id={} job_id={} job_type={} attempt={} failure_kind=retryable exception_type={}",
                        RequestCorrelation.forLog(current.requestId()),
                        id,
                        current.jobType(),
                        current.attemptCount(),
                        e.getClass().getSimpleName()
                );
                stateService.markFailed(
                        id,
                        leaseOwner,
                        clock.instant(),
                        false,
                        TRANSIENT_ERROR_CODE,
                        TRANSIENT_ERROR_SUMMARY
                );
            }
        }
    }
}
