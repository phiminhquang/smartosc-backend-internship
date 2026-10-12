package com.example.device.service.impl;

import com.example.device.enums.EmailJobStatus;
import com.example.device.enums.EmailJobType;
import com.example.device.model.EmailJob;
import com.example.device.observability.RequestCorrelation;
import com.example.device.repository.EmailJobRepository;
import com.example.device.service.EmailJobEnqueueCommand;
import com.example.device.service.EmailJobEnqueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailJobEnqueueServiceImpl implements EmailJobEnqueueService {

    private static final int TEMPLATE_VERSION = 1;
    private static final int MAX_ATTEMPTS = 5;
    private static final Set<EmailJobType> ASSIGNMENT_NOTIFICATION_TYPES = Set.of(
            EmailJobType.ASSIGNMENT_REMINDER,
            EmailJobType.ASSIGNMENT_OVERDUE
    );

    private final EmailJobRepository emailJobRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean enqueue(EmailJobEnqueueCommand command) {
        validate(command);
        String idempotencyKey = sha256(command.logicalKey());
        if (emailJobRepository.existsByIdempotencyKey(idempotencyKey)) {
            return false;
        }

        Instant now = clock.instant();
        EmailJob job = EmailJob.builder()
                .requestId(RequestCorrelation.currentId())
                .jobType(command.jobType())
                .recipient(command.recipient())
                .subject(command.subject())
                .templateKey(command.templateKey())
                .templateVersion(TEMPLATE_VERSION)
                .payload(writePayload(command))
                .assignmentId(command.assignmentId())
                .assignmentExpectedReturnAt(command.assignmentExpectedReturnAt())
                .idempotencyKey(idempotencyKey)
                .status(EmailJobStatus.PENDING)
                .attemptCount(0)
                .maxAttempts(MAX_ATTEMPTS)
                .availableAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
        emailJobRepository.save(job);
        log.info(
                "event=email_job_enqueued request_id={} job_id={} job_type={}",
                RequestCorrelation.forLog(job.getRequestId()),
                job.getId(),
                job.getJobType()
        );
        return true;
    }

    private void validate(EmailJobEnqueueCommand command) {
        if (command.jobType() == EmailJobType.PASSWORD_RESET) {
            throw new IllegalArgumentException("Password reset must use the encrypted enqueue service");
        }
        boolean assignmentNotification = ASSIGNMENT_NOTIFICATION_TYPES.contains(command.jobType());
        boolean hasAllAssignmentMetadata = command.assignmentId() != null
                && command.assignmentExpectedReturnAt() != null;
        boolean hasAnyAssignmentMetadata = command.assignmentId() != null
                || command.assignmentExpectedReturnAt() != null;
        if ((assignmentNotification && !hasAllAssignmentMetadata)
                || (!assignmentNotification && hasAnyAssignmentMetadata)) {
            throw new IllegalArgumentException("Assignment notification metadata does not match job type");
        }
        if (command.recipient() == null || command.recipient().isBlank()
                || command.subject() == null || command.subject().isBlank()
                || command.templateKey() == null || command.templateKey().isBlank()
                || command.variables() == null
                || command.logicalKey() == null || command.logicalKey().isBlank()) {
            throw new IllegalArgumentException("Email job enqueue command is incomplete");
        }
    }

    private String writePayload(EmailJobEnqueueCommand command) {
        try {
            return objectMapper.writeValueAsString(command.variables());
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot serialize email job snapshot", exception);
        }
    }

    private String sha256(String logicalKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(logicalKey.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
