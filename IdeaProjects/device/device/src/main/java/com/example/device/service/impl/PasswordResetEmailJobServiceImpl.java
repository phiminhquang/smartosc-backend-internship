package com.example.device.service.impl;

import com.example.device.enums.EmailJobStatus;
import com.example.device.enums.EmailJobType;
import com.example.device.model.EmailJob;
import com.example.device.model.PasswordResetToken;
import com.example.device.model.User;
import com.example.device.observability.RequestCorrelation;
import com.example.device.repository.EmailJobRepository;
import com.example.device.service.EmailJobSecretCipher;
import com.example.device.service.EncryptedEmailJobSecret;
import com.example.device.service.PasswordResetEmailJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetEmailJobServiceImpl implements PasswordResetEmailJobService {

    private static final String SUBJECT = "Đặt lại mật khẩu Device Management";
    private static final String TEMPLATE_KEY = "email/password-reset";
    private static final int TEMPLATE_VERSION = 1;
    private static final int MAX_ATTEMPTS = 4;

    private final EmailJobRepository emailJobRepository;
    private final EmailJobSecretCipher secretCipher;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(
            User user,
            PasswordResetToken resetToken,
            String rawToken,
            long expirationMinutes
    ) {
        if (resetToken.getId() == null) {
            throw new IllegalStateException("Password reset token must be persisted before enqueue");
        }

        EncryptedEmailJobSecret encrypted = secretCipher.encrypt(EmailJobType.PASSWORD_RESET, rawToken);
        Instant now = resetToken.getCreatedAt();
        EmailJob job = EmailJob.builder()
                .requestId(RequestCorrelation.currentId())
                .jobType(EmailJobType.PASSWORD_RESET)
                .recipient(user.getEmail())
                .subject(SUBJECT)
                .templateKey(TEMPLATE_KEY)
                .templateVersion(TEMPLATE_VERSION)
                .payload(writePayload(user.getName(), expirationMinutes))
                .secretCiphertext(encrypted.ciphertext())
                .secretNonce(encrypted.nonce())
                .secretKeyVersion(encrypted.keyVersion())
                .passwordResetTokenId(resetToken.getId())
                .idempotencyKey(hashIdempotencyKey(resetToken))
                .status(EmailJobStatus.PENDING)
                .attemptCount(0)
                .maxAttempts(MAX_ATTEMPTS)
                .availableAt(now)
                .expiresAt(resetToken.getExpiresAt())
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
    }

    private String writePayload(String name, long expirationMinutes) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "name", name,
                    "expirationMinutes", expirationMinutes
            ));
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot serialize password reset email snapshot", exception);
        }
    }

    private String hashIdempotencyKey(PasswordResetToken resetToken) {
        String logicalKey = "password-reset:" + resetToken.getId();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(logicalKey.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
