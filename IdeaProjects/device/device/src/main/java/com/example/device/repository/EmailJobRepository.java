package com.example.device.repository;

import com.example.device.model.EmailJob;
import com.example.device.enums.EmailJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailJobRepository extends JpaRepository<EmailJob, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<EmailJob> findByIdempotencyKey(String idempotencyKey);

    long countByStatus(EmailJobStatus status);

    @Query(value = """
            SELECT *
            FROM email_jobs
            WHERE status IN ('PENDING', 'RETRY_WAIT')
              AND available_at <= :now
              AND attempt_count < max_attempts
            ORDER BY available_at ASC, created_at ASC, id ASC
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<EmailJob> findClaimableForUpdate(
            @Param("now") Instant now,
            @Param("batchSize") int batchSize
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT job
            FROM EmailJob job
            WHERE job.id = :id
              AND job.status = com.example.device.enums.EmailJobStatus.PROCESSING
              AND job.leaseOwner = :leaseOwner
            """)
    Optional<EmailJob> findProcessingForUpdate(
            @Param("id") UUID id,
            @Param("leaseOwner") String leaseOwner
    );

    @Modifying
    @Query(value = """
            UPDATE email_jobs
            SET status = 'UNKNOWN',
                lease_owner = NULL,
                lease_until = NULL,
                secret_ciphertext = NULL,
                secret_nonce = NULL,
                secret_key_version = NULL,
                last_error_code = 'LEASE_EXPIRED',
                last_error_summary = 'Delivery outcome is unknown after the worker lease expired',
                updated_at = :now,
                version = version + 1
            WHERE status = 'PROCESSING'
              AND lease_until <= :now
            ORDER BY lease_until ASC
            LIMIT :batchSize
            """, nativeQuery = true)
    int recoverExpiredLeases(
            @Param("now") Instant now,
            @Param("batchSize") int batchSize
    );

    @Modifying
    @Query(value = """
            DELETE FROM email_jobs
            WHERE status IN ('SENT', 'EXPIRED')
              AND updated_at < :cutoff
            ORDER BY updated_at ASC
            LIMIT :batchSize
            """, nativeQuery = true)
    int deleteCompletedBefore(
            @Param("cutoff") Instant cutoff,
            @Param("batchSize") int batchSize
    );

    @Modifying
    @Query(value = """
            DELETE FROM email_jobs
            WHERE status IN ('DEAD', 'UNKNOWN')
              AND updated_at < :cutoff
            ORDER BY updated_at ASC
            LIMIT :batchSize
            """, nativeQuery = true)
    int deleteFailedBefore(
            @Param("cutoff") Instant cutoff,
            @Param("batchSize") int batchSize
    );
}
