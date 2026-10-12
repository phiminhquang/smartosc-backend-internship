package com.example.device.service;

import com.example.device.enums.EmailJobType;

import java.util.UUID;

public record EmailJobDelivery(
        UUID id,
        String requestId,
        EmailJobType jobType,
        String recipient,
        String subject,
        String templateKey,
        int templateVersion,
        String payload,
        byte[] secretCiphertext,
        byte[] secretNonce,
        String secretKeyVersion,
        UUID passwordResetTokenId,
        java.time.Instant expiresAt,
        int attemptCount,
        int maxAttempts
) {
}
