package com.example.device.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class RateLimitBucketRepository {

    private final JdbcTemplate jdbcTemplate;

    public int incrementAndGet(byte[] bucketKey, Instant now, Instant expiresAt) {
        Timestamp nowTimestamp = Timestamp.from(now);
        jdbcTemplate.update("""
                INSERT INTO rate_limit_buckets (
                    bucket_key, request_count, expires_at, created_at, updated_at
                ) VALUES (?, 1, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    request_count = LEAST(request_count + 1, 2147483647),
                    expires_at = VALUES(expires_at),
                    updated_at = VALUES(updated_at)
                """,
                bucketKey,
                Timestamp.from(expiresAt),
                nowTimestamp,
                nowTimestamp
        );

        Integer count = jdbcTemplate.queryForObject(
                "SELECT request_count FROM rate_limit_buckets WHERE bucket_key = ?",
                Integer.class,
                bucketKey
        );
        if (count == null) {
            throw new IllegalStateException("Rate-limit bucket was not persisted");
        }
        return count;
    }

    public int deleteExpired(Instant now, int batchSize) {
        return jdbcTemplate.update("""
                DELETE FROM rate_limit_buckets
                WHERE expires_at <= ?
                ORDER BY expires_at ASC
                LIMIT ?
                """, Timestamp.from(now), batchSize);
    }
}
