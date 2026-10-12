package com.example.device;

import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.enums.DeviceCategory;
import com.example.device.enums.DeviceState;
import com.example.device.enums.EmailJobStatus;
import com.example.device.enums.EmailJobType;
import com.example.device.model.Device;
import com.example.device.model.DeviceAssignment;
import com.example.device.model.EmailJob;
import com.example.device.model.PasswordResetToken;
import com.example.device.model.User;
import com.example.device.repository.DeviceAssignmentRepository;
import com.example.device.repository.DeviceRepository;
import com.example.device.repository.EmailJobRepository;
import com.example.device.repository.PasswordResetTokenRepository;
import com.example.device.repository.UserRepository;
import com.example.device.scheduler.EmailJobWorker;
import com.example.device.service.EmailJobClaimService;
import com.example.device.service.EmailJobPermanentException;
import com.example.device.service.EmailJobSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
class EmailJobClaimIntegrationTest {

    private static final String ISOLATED_JDBC_URL = "jdbc:tc:mysql:8.4:///device_email_job_test";
    private static final String TEST_ADMIN_PASSWORD = UUID.randomUUID().toString();
    private static final String TEST_JWT_KEY = Base64.getEncoder()
            .encodeToString(SecureRandom.getSeed(64));

    @Autowired
    private EmailJobRepository emailJobRepository;
    @Autowired
    private EmailJobClaimService emailJobClaimService;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private DeviceAssignmentRepository deviceAssignmentRepository;
    @Autowired
    private EmailJobWorker emailJobWorker;
    @MockitoBean
    private EmailJobSender emailJobSender;

    @DynamicPropertySource
    static void testProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> ISOLATED_JDBC_URL);
        registry.add("spring.datasource.driver-class-name", () -> "org.testcontainers.jdbc.ContainerDatabaseDriver");
        registry.add("spring.datasource.username", () -> "");
        registry.add("spring.datasource.password", () -> "");
        registry.add("app.admin.password", () -> TEST_ADMIN_PASSWORD);
        registry.add("jwt.signer-key", () -> TEST_JWT_KEY);
    }

    @BeforeEach
    void clearJobs() {
        emailJobRepository.deleteAll();
        reset(emailJobSender);
    }

    @Test
    void workerSendsDueJobOnceAndMarksItSent() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        EmailJob saved = emailJobRepository.saveAndFlush(job(
                EmailJobStatus.PENDING,
                now.minusSeconds(1),
                "success"
        ));

        assertEquals(1, emailJobWorker.processBatch());
        assertEquals(0, emailJobWorker.processBatch());

        EmailJob completed = emailJobRepository.findById(saved.getId()).orElseThrow();
        assertEquals(EmailJobStatus.SENT, completed.getStatus());
        assertEquals(1, completed.getAttemptCount());
        assertNull(completed.getLeaseOwner());
        assertNull(completed.getLeaseUntil());
        assertTrue(completed.getSentAt() != null);
        verify(emailJobSender, times(1)).send(any());
    }

    @Test
    void workerMarksAssignmentNotificationsOnlyAfterSuccessfulDelivery() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        DeviceAssignment reminder = assignment(
                DeviceAssignmentStatus.ACTIVE,
                LocalDateTime.now().plusHours(2),
                "reminder-marker"
        );
        DeviceAssignment overdue = assignment(
                DeviceAssignmentStatus.OVERDUE,
                LocalDateTime.now().minusDays(1),
                "overdue-marker"
        );
        emailJobRepository.saveAndFlush(assignmentJob(
                EmailJobType.ASSIGNMENT_REMINDER, reminder, now.minusSeconds(1), "reminder-marker"));
        emailJobRepository.saveAndFlush(assignmentJob(
                EmailJobType.ASSIGNMENT_OVERDUE, overdue, now.minusSeconds(1), "overdue-marker"));

        assertEquals(2, emailJobWorker.processBatch());

        DeviceAssignment sentReminder = deviceAssignmentRepository.findById(reminder.getId()).orElseThrow();
        DeviceAssignment sentOverdue = deviceAssignmentRepository.findById(overdue.getId()).orElseThrow();
        assertTrue(sentReminder.getReminderNotifiedAt() != null);
        assertTrue(sentOverdue.getOverdueNotifiedAt() != null);
        verify(emailJobSender, times(2)).send(any());
    }

    @Test
    void staleAssignmentReminderExpiresWithoutSendingOrMarkingCurrentDeadline() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        LocalDateTime originalDeadline = LocalDateTime.now().plusHours(2);
        DeviceAssignment assignment = assignment(
                DeviceAssignmentStatus.ACTIVE,
                originalDeadline,
                "stale-reminder"
        );
        EmailJob job = emailJobRepository.saveAndFlush(assignmentJob(
                EmailJobType.ASSIGNMENT_REMINDER,
                assignment,
                now.minusSeconds(1),
                "stale-reminder"
        ));
        assignment.setExpectedReturnAt(originalDeadline.plusDays(2));
        deviceAssignmentRepository.saveAndFlush(assignment);

        assertEquals(1, emailJobWorker.processBatch());

        EmailJob expired = emailJobRepository.findById(job.getId()).orElseThrow();
        DeviceAssignment current = deviceAssignmentRepository.findById(assignment.getId()).orElseThrow();
        assertEquals(EmailJobStatus.EXPIRED, expired.getStatus());
        assertNull(current.getReminderNotifiedAt());
        verify(emailJobSender, never()).send(any());
    }

    @Test
    void failedAssignmentNotificationKeepsMarkerUnsetForRetry() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        DeviceAssignment assignment = assignment(
                DeviceAssignmentStatus.ACTIVE,
                LocalDateTime.now().plusHours(2),
                "retry-reminder"
        );
        EmailJob job = emailJobRepository.saveAndFlush(assignmentJob(
                EmailJobType.ASSIGNMENT_REMINDER,
                assignment,
                now.minusSeconds(1),
                "retry-reminder"
        ));
        doThrow(new IllegalStateException("smtp unavailable")).when(emailJobSender).send(any());

        assertEquals(1, emailJobWorker.processBatch());

        assertEquals(EmailJobStatus.RETRY_WAIT,
                emailJobRepository.findById(job.getId()).orElseThrow().getStatus());
        assertNull(deviceAssignmentRepository.findById(assignment.getId()).orElseThrow().getReminderNotifiedAt());
    }

    @Test
    void retryableFailureUsesBackoffAndRedactedError() {
        Instant before = Instant.now().truncatedTo(ChronoUnit.MICROS);
        EmailJob saved = emailJobRepository.saveAndFlush(job(
                EmailJobStatus.PENDING,
                before.minusSeconds(1),
                "retry"
        ));
        doThrow(new IllegalStateException("raw secret must never reach the database"))
                .when(emailJobSender).send(any());

        assertEquals(1, emailJobWorker.processBatch());

        Instant after = Instant.now().truncatedTo(ChronoUnit.MICROS);
        EmailJob retry = emailJobRepository.findById(saved.getId()).orElseThrow();
        assertEquals(EmailJobStatus.RETRY_WAIT, retry.getStatus());
        assertEquals(1, retry.getAttemptCount());
        assertTrue(retry.getAvailableAt().compareTo(before.plusSeconds(60)) >= 0);
        assertTrue(retry.getAvailableAt().compareTo(after.plusSeconds(66)) <= 0);
        assertEquals("EMAIL_SEND_FAILED", retry.getLastErrorCode());
        assertFalse(retry.getLastErrorSummary().contains("raw secret"));
        assertNull(retry.getLeaseOwner());
    }

    @Test
    void permanentFailureBecomesDeadAndClearsSecret() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        EmailJob pending = job(EmailJobStatus.PENDING, now.minusSeconds(1), "dead");
        pending.setSecretCiphertext(new byte[]{1, 2, 3});
        pending.setSecretNonce(new byte[12]);
        pending.setSecretKeyVersion("test-key-v1");
        EmailJob saved = emailJobRepository.saveAndFlush(pending);
        doThrow(new EmailJobPermanentException("payload contains a secret"))
                .when(emailJobSender).send(any());

        assertEquals(1, emailJobWorker.processBatch());

        EmailJob dead = emailJobRepository.findById(saved.getId()).orElseThrow();
        assertEquals(EmailJobStatus.DEAD, dead.getStatus());
        assertEquals("INVALID_EMAIL_JOB", dead.getLastErrorCode());
        assertFalse(dead.getLastErrorSummary().contains("secret"));
        assertNull(dead.getSecretCiphertext());
        assertNull(dead.getSecretNonce());
        assertNull(dead.getSecretKeyVersion());
    }

    @Test
    void retryableFailureStopsAtConfiguredMaximumAttempts() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        EmailJob pending = job(EmailJobStatus.PENDING, now.minusSeconds(1), "max-attempts");
        pending.setMaxAttempts(1);
        EmailJob saved = emailJobRepository.saveAndFlush(pending);
        doThrow(new IllegalStateException("SMTP unavailable"))
                .when(emailJobSender).send(any());

        assertEquals(1, emailJobWorker.processBatch());

        EmailJob dead = emailJobRepository.findById(saved.getId()).orElseThrow();
        assertEquals(EmailJobStatus.DEAD, dead.getStatus());
        assertEquals(1, dead.getAttemptCount());
        assertEquals("EMAIL_SEND_FAILED", dead.getLastErrorCode());
        assertNull(dead.getLeaseOwner());
    }

    @Test
    void expiredLeaseBecomesUnknownWithoutAutomaticRetry() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        EmailJob processing = job(EmailJobStatus.PROCESSING, now.minusSeconds(120), "unknown");
        processing.setAttemptCount(1);
        processing.setLeaseOwner("crashed-worker");
        processing.setLeaseUntil(now.minusSeconds(1));
        processing.setSecretCiphertext(new byte[]{1, 2, 3});
        processing.setSecretNonce(new byte[12]);
        processing.setSecretKeyVersion("test-key-v1");
        EmailJob saved = emailJobRepository.saveAndFlush(processing);

        assertEquals(1, emailJobWorker.recoverExpiredLeases());
        assertEquals(0, emailJobWorker.processBatch());

        EmailJob unknown = emailJobRepository.findById(saved.getId()).orElseThrow();
        assertEquals(EmailJobStatus.UNKNOWN, unknown.getStatus());
        assertEquals("LEASE_EXPIRED", unknown.getLastErrorCode());
        assertNull(unknown.getSecretCiphertext());
        verify(emailJobSender, times(0)).send(any());
    }

    @Test
    void cleanupHonorsApprovedRetentionWindows() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        EmailJob oldSent = job(EmailJobStatus.SENT, now.minus(31, ChronoUnit.DAYS), "old-sent");
        oldSent.setUpdatedAt(now.minus(31, ChronoUnit.DAYS));
        oldSent.setSentAt(oldSent.getUpdatedAt());
        EmailJob oldUnknown = job(EmailJobStatus.UNKNOWN, now.minus(91, ChronoUnit.DAYS), "old-unknown");
        oldUnknown.setUpdatedAt(now.minus(91, ChronoUnit.DAYS));
        EmailJob recentDead = job(EmailJobStatus.DEAD, now.minus(1, ChronoUnit.DAYS), "recent-dead");
        recentDead.setUpdatedAt(now.minus(1, ChronoUnit.DAYS));
        List<EmailJob> saved = emailJobRepository.saveAllAndFlush(List.of(oldSent, oldUnknown, recentDead));

        assertEquals(2, emailJobWorker.cleanup());

        assertFalse(emailJobRepository.existsById(saved.get(0).getId()));
        assertFalse(emailJobRepository.existsById(saved.get(1).getId()));
        assertTrue(emailJobRepository.existsById(saved.get(2).getId()));
    }

    @Test
    void expiredPasswordResetJobExpiresAndInvalidatesTokenWithoutSending() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        PasswordResetToken token = passwordResetToken(now.minusSeconds(1), "expired-reset");
        EmailJob saved = emailJobRepository.saveAndFlush(passwordResetJob(
                EmailJobStatus.PENDING,
                token,
                now.minusSeconds(120),
                "expired-reset"
        ));

        assertEquals(1, emailJobWorker.processBatch());

        EmailJob expired = emailJobRepository.findById(saved.getId()).orElseThrow();
        PasswordResetToken invalidated = passwordResetTokenRepository.findById(token.getId()).orElseThrow();
        assertEquals(EmailJobStatus.EXPIRED, expired.getStatus());
        assertEquals("RESET_TOKEN_INACTIVE", expired.getLastErrorCode());
        assertNull(expired.getSecretCiphertext());
        assertTrue(invalidated.getUsedAt() != null);
        verify(emailJobSender, times(0)).send(any());
    }

    @Test
    void deadPasswordResetJobInvalidatesToken() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        PasswordResetToken token = passwordResetToken(now.plusSeconds(300), "dead-reset");
        EmailJob pending = passwordResetJob(
                EmailJobStatus.PENDING,
                token,
                now.minusSeconds(1),
                "dead-reset"
        );
        pending.setMaxAttempts(1);
        EmailJob saved = emailJobRepository.saveAndFlush(pending);
        doThrow(new IllegalStateException("SMTP unavailable"))
                .when(emailJobSender).send(any());

        assertEquals(1, emailJobWorker.processBatch());

        EmailJob dead = emailJobRepository.findById(saved.getId()).orElseThrow();
        PasswordResetToken invalidated = passwordResetTokenRepository.findById(token.getId()).orElseThrow();
        assertEquals(EmailJobStatus.DEAD, dead.getStatus());
        assertTrue(invalidated.getUsedAt() != null);
    }

    @Test
    void unknownPasswordResetDeliveryKeepsTokenUsableUntilExpiry() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        PasswordResetToken token = passwordResetToken(now.plusSeconds(300), "unknown-reset");
        EmailJob processing = passwordResetJob(
                EmailJobStatus.PROCESSING,
                token,
                now.minusSeconds(120),
                "unknown-reset"
        );
        processing.setAttemptCount(1);
        processing.setLeaseOwner("crashed-worker");
        processing.setLeaseUntil(now.minusSeconds(1));
        EmailJob saved = emailJobRepository.saveAndFlush(processing);

        assertEquals(1, emailJobWorker.recoverExpiredLeases());

        assertEquals(EmailJobStatus.UNKNOWN,
                emailJobRepository.findById(saved.getId()).orElseThrow().getStatus());
        assertNull(passwordResetTokenRepository.findById(token.getId()).orElseThrow().getUsedAt());
    }

    @Test
    void concurrentWorkersClaimDisjointDueBatches() throws Exception {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        for (int index = 0; index < 4; index++) {
            emailJobRepository.save(job(
                    EmailJobStatus.PENDING,
                    now.minusSeconds(1),
                    "due-" + index
            ));
        }
        EmailJob futureJob = emailJobRepository.save(job(
                EmailJobStatus.PENDING,
                now.plusSeconds(300),
                "future"
        ));
        EmailJob sentJob = emailJobRepository.save(job(
                EmailJobStatus.SENT,
                now.minusSeconds(1),
                "sent"
        ));
        emailJobRepository.flush();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<List<UUID>> first = executor.submit(() -> claim("worker-a", now, ready, start));
            Future<List<UUID>> second = executor.submit(() -> claim("worker-b", now, ready, start));

            assertTrue(ready.await(10, TimeUnit.SECONDS), "Workers did not become ready");
            start.countDown();

            List<UUID> firstIds = first.get(30, TimeUnit.SECONDS);
            List<UUID> secondIds = second.get(30, TimeUnit.SECONDS);

            assertTrue(firstIds.size() <= 2);
            assertTrue(secondIds.size() <= 2);
            Set<UUID> claimedIds = new HashSet<>(firstIds);
            assertTrue(claimedIds.stream().noneMatch(secondIds::contains));
            claimedIds.addAll(secondIds);

            List<UUID> remainingIds = emailJobClaimService.claimBatch(
                    "worker-c",
                    now,
                    Duration.ofSeconds(60),
                    4
            );
            assertTrue(claimedIds.stream().noneMatch(remainingIds::contains));
            claimedIds.addAll(remainingIds);
            assertEquals(4, claimedIds.size());

            List<EmailJob> claimedJobs = emailJobRepository.findAllById(claimedIds);
            assertTrue(claimedJobs.stream().allMatch(job -> job.getStatus() == EmailJobStatus.PROCESSING));
            assertTrue(claimedJobs.stream().allMatch(job -> job.getAttemptCount() == 1));
            assertTrue(claimedJobs.stream().allMatch(job -> Set.of("worker-a", "worker-b", "worker-c")
                    .contains(job.getLeaseOwner())));
            assertTrue(claimedJobs.stream().allMatch(job -> now.plusSeconds(60).equals(job.getLeaseUntil())));

            assertEquals(EmailJobStatus.PENDING,
                    emailJobRepository.findById(futureJob.getId()).orElseThrow().getStatus());
            assertEquals(EmailJobStatus.SENT,
                    emailJobRepository.findById(sentJob.getId()).orElseThrow().getStatus());
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private List<UUID> claim(
            String worker,
            Instant now,
            CountDownLatch ready,
            CountDownLatch start
    ) throws Exception {
        ready.countDown();
        assertTrue(start.await(10, TimeUnit.SECONDS), "Concurrent start signal timed out");
        return emailJobClaimService.claimBatch(worker, now, Duration.ofSeconds(60), 2);
    }

    private EmailJob job(EmailJobStatus status, Instant availableAt, String suffix) {
        Instant createdAt = availableAt.minusSeconds(1);
        String idempotencyKey = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        return EmailJob.builder()
                .jobType(EmailJobType.DAILY_OVERDUE_REPORT)
                .recipient(suffix + "@device.test")
                .subject("Email job integration test")
                .templateKey("email/device-reminder")
                .templateVersion(1)
                .payload("{}")
                .idempotencyKey(idempotencyKey)
                .status(status)
                .attemptCount(0)
                .maxAttempts(5)
                .availableAt(availableAt)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .sentAt(status == EmailJobStatus.SENT ? createdAt : null)
                .build();
    }

    private PasswordResetToken passwordResetToken(Instant expiresAt, String suffix) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        User user = userRepository.saveAndFlush(User.builder()
                .email(suffix + "-" + UUID.randomUUID() + "@device.test")
                .name("Password Reset Test")
                .password("test-password-hash")
                .build());
        return passwordResetTokenRepository.saveAndFlush(PasswordResetToken.builder()
                .user(user)
                .tokenHash(idempotencyKey())
                .createdAt(now.minusSeconds(30))
                .expiresAt(expiresAt)
                .build());
    }

    private DeviceAssignment assignment(
            DeviceAssignmentStatus status,
            LocalDateTime expectedReturnAt,
            String suffix
    ) {
        User user = userRepository.saveAndFlush(User.builder()
                .email(suffix + "-" + UUID.randomUUID() + "@device.test")
                .name("Assignment Email Test")
                .password("test-password-hash")
                .build());
        Device device = deviceRepository.saveAndFlush(Device.builder()
                .category(DeviceCategory.LAPTOP)
                .serialNumber(("EJ-" + UUID.randomUUID()).substring(0, 20))
                .name("Email Job Laptop")
                .model("Test Model")
                .state(DeviceState.ASSIGNED)
                .build());
        return deviceAssignmentRepository.saveAndFlush(DeviceAssignment.builder()
                .user(user)
                .device(device)
                .assignedBy("integration@device.test")
                .assignedAt(expectedReturnAt.minusDays(7))
                .expectedReturnAt(expectedReturnAt)
                .status(status)
                .build());
    }

    private EmailJob assignmentJob(
            EmailJobType type,
            DeviceAssignment assignment,
            Instant availableAt,
            String suffix
    ) {
        Instant createdAt = availableAt.minusSeconds(1);
        return EmailJob.builder()
                .jobType(type)
                .recipient(suffix + "@device.test")
                .subject("Assignment notification")
                .templateKey(type == EmailJobType.ASSIGNMENT_REMINDER
                        ? "email/device-reminder"
                        : "email/device-overdue")
                .templateVersion(1)
                .payload("{}")
                .assignmentId(assignment.getId())
                .assignmentExpectedReturnAt(assignment.getExpectedReturnAt())
                .idempotencyKey(idempotencyKey())
                .status(EmailJobStatus.PENDING)
                .attemptCount(0)
                .maxAttempts(5)
                .availableAt(availableAt)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }

    private EmailJob passwordResetJob(
            EmailJobStatus status,
            PasswordResetToken token,
            Instant availableAt,
            String suffix
    ) {
        Instant createdAt = availableAt.minusSeconds(1);
        return EmailJob.builder()
                .jobType(EmailJobType.PASSWORD_RESET)
                .recipient(suffix + "@device.test")
                .subject("Password reset")
                .templateKey("email/password-reset")
                .templateVersion(1)
                .payload("{\"name\":\"Test User\",\"expirationMinutes\":15}")
                .secretCiphertext(new byte[]{1, 2, 3})
                .secretNonce(new byte[12])
                .secretKeyVersion("v1")
                .passwordResetTokenId(token.getId())
                .idempotencyKey(idempotencyKey())
                .status(status)
                .attemptCount(0)
                .maxAttempts(4)
                .availableAt(availableAt)
                .expiresAt(token.getExpiresAt())
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }

    private String idempotencyKey() {
        return UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
    }
}
