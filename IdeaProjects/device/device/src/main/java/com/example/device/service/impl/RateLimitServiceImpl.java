package com.example.device.service.impl;

import com.example.device.configuration.RateLimitProperties;
import com.example.device.exception.RateLimitExceededException;
import com.example.device.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private final RateLimitCounterStore counterStore;
    private final RateLimitProperties properties;
    private final Clock clock;

    @Override
    public void checkLogin(String email, String clientAddress) {
        check("login", email, clientAddress, properties.getLogin());
    }

    @Override
    public void checkPasswordResetRequest(String email, String clientAddress) {
        check("password-reset-request", email, clientAddress, properties.getPasswordReset());
    }

    @Override
    public int cleanupExpired(Instant now, int batchSize) {
        return counterStore.deleteExpired(now, batchSize);
    }

    private void check(
            String action,
            String email,
            String clientAddress,
            RateLimitProperties.Policy policy
    ) {
        Instant now = clock.instant();
        long windowSeconds = policy.getWindow().getSeconds();
        long windowStartEpoch = Math.floorDiv(now.getEpochSecond(), windowSeconds) * windowSeconds;
        Instant windowEnd = Instant.ofEpochSecond(Math.addExact(windowStartEpoch, windowSeconds));

        consume(
                action,
                "ip",
                normalizeClientAddress(clientAddress),
                policy.getIpLimit(),
                now,
                windowStartEpoch,
                windowEnd
        );
        consume(
                action,
                "identity",
                normalizeEmail(email),
                policy.getIdentityLimit(),
                now,
                windowStartEpoch,
                windowEnd
        );
    }

    private void consume(
            String action,
            String scope,
            String discriminator,
            int limit,
            Instant now,
            long windowStartEpoch,
            Instant windowEnd
    ) {
        byte[] bucketKey = hash(action, scope, discriminator, windowStartEpoch);
        int count = counterStore.incrementAndGet(bucketKey, now, windowEnd);
        if (count > limit) {
            long retryAfterSeconds = Math.max(1, Duration.between(now, windowEnd).getSeconds());
            throw new RateLimitExceededException(retryAfterSeconds);
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeClientAddress(String clientAddress) {
        return clientAddress == null || clientAddress.isBlank() ? "unknown" : clientAddress.trim();
    }

    private byte[] hash(String action, String scope, String discriminator, long windowStartEpoch) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String material = action + '\n' + scope + '\n' + discriminator + '\n' + windowStartEpoch;
            return digest.digest(material.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
