package com.example.device.model;

import com.example.device.enums.EmailJobStatus;
import com.example.device.enums.EmailJobType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_jobs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailJob {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "request_id", length = 64)
    private String requestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 64)
    private EmailJobType jobType;

    @Column(nullable = false, length = 320)
    private String recipient;

    @Column(nullable = false)
    private String subject;

    @Column(name = "template_key", nullable = false, length = 128)
    private String templateKey;

    @Column(name = "template_version", nullable = false)
    private int templateVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "json")
    private String payload;

    @Column(name = "secret_ciphertext", columnDefinition = "varbinary(4096)")
    private byte[] secretCiphertext;

    @Column(name = "secret_nonce", columnDefinition = "varbinary(12)")
    private byte[] secretNonce;

    @Column(name = "secret_key_version", length = 64)
    private String secretKeyVersion;

    @Column(name = "password_reset_token_id", columnDefinition = "binary(16)")
    private UUID passwordResetTokenId;

    @Column(name = "assignment_id", columnDefinition = "binary(16)")
    private UUID assignmentId;

    @Column(name = "assignment_expected_return_at")
    private LocalDateTime assignmentExpectedReturnAt;

    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private EmailJobStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "lease_owner", length = 128)
    private String leaseOwner;

    @Column(name = "lease_until")
    private Instant leaseUntil;

    @Column(name = "last_error_code", length = 64)
    private String lastErrorCode;

    @Column(name = "last_error_summary", length = 500)
    private String lastErrorSummary;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Version
    @Column(nullable = false)
    private long version;
}
