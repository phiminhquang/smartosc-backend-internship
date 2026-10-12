package com.example.device.service.impl;

import com.example.device.configuration.EmailJobProperties;
import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.enums.EmailJobStatus;
import com.example.device.enums.EmailJobType;
import com.example.device.model.DeviceAssignment;
import com.example.device.model.EmailJob;
import com.example.device.model.PasswordResetToken;
import com.example.device.repository.DeviceAssignmentRepository;
import com.example.device.repository.EmailJobRepository;
import com.example.device.repository.PasswordResetTokenRepository;
import com.example.device.service.EmailJobDelivery;
import com.example.device.service.EmailJobRetryPolicy;
import com.example.device.service.EmailJobStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailJobStateServiceImpl implements EmailJobStateService {

    private static final int MAX_ERROR_CODE_LENGTH = 64;
    private static final int MAX_ERROR_SUMMARY_LENGTH = 500;
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final EmailJobRepository emailJobRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final DeviceAssignmentRepository deviceAssignmentRepository;
    private final EmailJobRetryPolicy retryPolicy;
    private final EmailJobProperties properties;

    @Override
    @Transactional
    public Optional<EmailJobDelivery> loadForDelivery(
            UUID id,
            String leaseOwner,
            Instant now
    ) {
        Optional<EmailJob> current = currentLease(id, leaseOwner, now);
        if (current.isEmpty()) {
            return Optional.empty();
        }
        EmailJob job = current.get();
        if (expireInactivePasswordReset(job, now)) {
            emailJobRepository.flush();
            return Optional.empty();
        }
        if (expireInactiveAssignmentNotification(job, now)) {
            emailJobRepository.flush();
            return Optional.empty();
        }
        return Optional.of(toDelivery(job));
    }

    @Override
    @Transactional
    public boolean markSent(UUID id, String leaseOwner, Instant now) {
        Optional<EmailJob> current = currentLease(id, leaseOwner, now);
        if (current.isEmpty()) {
            return false;
        }

        EmailJob job = current.get();
        markAssignmentNotificationSent(job, now);
        job.setStatus(EmailJobStatus.SENT);
        job.setSentAt(now);
        job.setUpdatedAt(now);
        job.setLeaseOwner(null);
        job.setLeaseUntil(null);
        job.setLastErrorCode(null);
        job.setLastErrorSummary(null);
        clearSecret(job);
        emailJobRepository.flush();
        return true;
    }

    @Override
    @Transactional
    public boolean markFailed(
            UUID id,
            String leaseOwner,
            Instant now,
            boolean permanent,
            String errorCode,
            String errorSummary
    ) {
        Optional<EmailJob> current = currentLease(id, leaseOwner, now);
        if (current.isEmpty()) {
            return false;
        }

        EmailJob job = current.get();
        boolean terminal = permanent || job.getAttemptCount() >= job.getMaxAttempts();
        job.setStatus(terminal ? EmailJobStatus.DEAD : EmailJobStatus.RETRY_WAIT);
        job.setAvailableAt(terminal
                ? now
                : retryPolicy.nextAttemptAt(job.getId(), job.getJobType(), job.getAttemptCount(), now));
        job.setLeaseOwner(null);
        job.setLeaseUntil(null);
        job.setLastErrorCode(truncate(errorCode, MAX_ERROR_CODE_LENGTH));
        job.setLastErrorSummary(truncate(errorSummary, MAX_ERROR_SUMMARY_LENGTH));
        job.setUpdatedAt(now);
        if (terminal) {
            clearSecret(job);
            invalidatePasswordResetToken(job, now);
        }
        emailJobRepository.flush();
        return true;
    }

    @Override
    @Transactional
    public int recoverExpiredLeases(Instant now, int batchSize) {
        return emailJobRepository.recoverExpiredLeases(now, batchSize);
    }

    @Override
    @Transactional
    public int cleanup(Instant now, int batchSize) {
        int completed = emailJobRepository.deleteCompletedBefore(
                now.minus(properties.getCompletedRetention()),
                batchSize
        );
        int remainingCapacity = Math.max(0, batchSize - completed);
        int failed = remainingCapacity == 0 ? 0 : emailJobRepository.deleteFailedBefore(
                now.minus(properties.getFailedRetention()),
                remainingCapacity
        );
        return completed + failed;
    }

    private Optional<EmailJob> currentLease(UUID id, String leaseOwner, Instant now) {
        return emailJobRepository.findProcessingForUpdate(id, leaseOwner)
                .filter(job -> job.getLeaseUntil() != null && job.getLeaseUntil().isAfter(now));
    }

    private EmailJobDelivery toDelivery(EmailJob job) {
        return new EmailJobDelivery(
                job.getId(),
                job.getRequestId(),
                job.getJobType(),
                job.getRecipient(),
                job.getSubject(),
                job.getTemplateKey(),
                job.getTemplateVersion(),
                job.getPayload(),
                job.getSecretCiphertext(),
                job.getSecretNonce(),
                job.getSecretKeyVersion(),
                job.getPasswordResetTokenId(),
                job.getExpiresAt(),
                job.getAttemptCount(),
                job.getMaxAttempts()
        );
    }

    private void clearSecret(EmailJob job) {
        job.setSecretCiphertext(null);
        job.setSecretNonce(null);
        job.setSecretKeyVersion(null);
    }

    private boolean expireInactivePasswordReset(EmailJob job, Instant now) {
        if (job.getJobType() != EmailJobType.PASSWORD_RESET) {
            return false;
        }

        Optional<PasswordResetToken> token = passwordResetToken(job);
        boolean inactive = job.getExpiresAt() == null
                || !job.getExpiresAt().isAfter(now)
                || token.isEmpty()
                || token.get().getUsedAt() != null
                || !token.get().getExpiresAt().isAfter(now);
        if (!inactive) {
            return false;
        }

        job.setStatus(EmailJobStatus.EXPIRED);
        job.setLeaseOwner(null);
        job.setLeaseUntil(null);
        job.setLastErrorCode("RESET_TOKEN_INACTIVE");
        job.setLastErrorSummary("Password reset email expired before delivery");
        job.setUpdatedAt(now);
        clearSecret(job);
        token.filter(value -> value.getUsedAt() == null).ifPresent(value -> value.setUsedAt(now));
        return true;
    }

    private void invalidatePasswordResetToken(EmailJob job, Instant now) {
        if (job.getJobType() == EmailJobType.PASSWORD_RESET) {
            passwordResetToken(job)
                    .filter(token -> token.getUsedAt() == null)
                    .ifPresent(token -> token.setUsedAt(now));
        }
    }

    private boolean expireInactiveAssignmentNotification(EmailJob job, Instant now) {
        if (!isAssignmentNotification(job)) {
            return false;
        }

        Optional<DeviceAssignment> assignment = assignment(job);
        if (assignment.filter(value -> isCurrentAssignmentNotification(job, value)).isPresent()) {
            return false;
        }

        job.setStatus(EmailJobStatus.EXPIRED);
        job.setLeaseOwner(null);
        job.setLeaseUntil(null);
        job.setLastErrorCode("ASSIGNMENT_NOTIFICATION_STALE");
        job.setLastErrorSummary("Assignment notification no longer matches current state");
        job.setUpdatedAt(now);
        clearSecret(job);
        return true;
    }

    private void markAssignmentNotificationSent(EmailJob job, Instant now) {
        if (!isAssignmentNotification(job)) {
            return;
        }
        assignment(job)
                .filter(value -> isCurrentAssignmentNotification(job, value))
                .ifPresent(value -> {
                    LocalDateTime sentAt = LocalDateTime.ofInstant(now, VN_ZONE);
                    if (job.getJobType() == EmailJobType.ASSIGNMENT_REMINDER) {
                        value.setReminderNotifiedAt(sentAt);
                    } else {
                        value.setOverdueNotifiedAt(sentAt);
                    }
                });
    }

    private boolean isAssignmentNotification(EmailJob job) {
        return job.getJobType() == EmailJobType.ASSIGNMENT_REMINDER
                || job.getJobType() == EmailJobType.ASSIGNMENT_OVERDUE;
    }

    private Optional<DeviceAssignment> assignment(EmailJob job) {
        if (job.getAssignmentId() == null) {
            return Optional.empty();
        }
        return deviceAssignmentRepository.findByIdForUpdate(job.getAssignmentId());
    }

    private boolean isCurrentAssignmentNotification(EmailJob job, DeviceAssignment assignment) {
        if (!assignment.getExpectedReturnAt().equals(job.getAssignmentExpectedReturnAt())) {
            return false;
        }
        if (job.getJobType() == EmailJobType.ASSIGNMENT_REMINDER) {
            return assignment.getStatus() == DeviceAssignmentStatus.ACTIVE
                    && assignment.getReminderNotifiedAt() == null;
        }
        return assignment.getStatus() == DeviceAssignmentStatus.OVERDUE
                && assignment.getOverdueNotifiedAt() == null;
    }

    private Optional<PasswordResetToken> passwordResetToken(EmailJob job) {
        if (job.getPasswordResetTokenId() == null) {
            return Optional.empty();
        }
        return passwordResetTokenRepository.findByIdForUpdate(job.getPasswordResetTokenId());
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
