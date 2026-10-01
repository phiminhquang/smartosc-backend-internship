package com.example.device.repository;

import com.example.device.model.PasswordResetToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    boolean existsByUser_IdAndCreatedAtAfter(UUID userId, Instant threshold);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PasswordResetToken t join fetch t.user where t.tokenHash = :tokenHash")
    Optional<PasswordResetToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("""
            update PasswordResetToken t
            set t.usedAt = :usedAt
            where t.user.id = :userId
              and t.usedAt is null
            """)
    int markAllUnusedTokensAsUsed(
            @Param("userId") UUID userId,
            @Param("usedAt") Instant usedAt
    );

    @Modifying
    @Query("""
            update PasswordResetToken t
            set t.usedAt = :usedAt
            where t.user.id = :userId
              and t.usedAt is null
              and t.id <> :exceptId
            """)
    int markOtherUnusedTokensAsUsed(
            @Param("userId") UUID userId,
            @Param("exceptId") UUID exceptId,
            @Param("usedAt") Instant usedAt
    );
}
