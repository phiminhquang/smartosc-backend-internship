package com.example.device.service.impl;

import com.example.device.dto.request.PasswordResetConfirmRequest;
import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import com.example.device.model.PasswordResetToken;
import com.example.device.model.User;
import com.example.device.repository.PasswordResetTokenRepository;
import com.example.device.repository.UserRepository;
import com.example.device.service.EmailService;
import com.example.device.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final int TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.password-reset.base-url}")
    private String resetBaseUrl;

    @Value("${app.password-reset.expiration-minutes:15}")
    private long expirationMinutes;

    @Value("${app.password-reset.cooldown-seconds:60}")
    private long cooldownSeconds;

    @Override
    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmailForUpdate(email).ifPresent(this::createAndSendToken);
    }

    @Override
    @Transactional
    public void confirmReset(PasswordResetConfirmRequest request) {
        Instant now = clock.instant();
        PasswordResetToken resetToken = tokenRepository
                .findByTokenHashForUpdate(hashToken(request.getToken()))
                .orElseThrow(this::invalidToken);

        if (resetToken.getUsedAt() != null || !resetToken.getExpiresAt().isAfter(now)) {
            throw invalidToken();
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setTokenVersion(Math.addExact(user.getTokenVersion(), 1));

        resetToken.setUsedAt(now);
        tokenRepository.markOtherUnusedTokensAsUsed(user.getId(), resetToken.getId(), now);
    }

    private void createAndSendToken(User user) {
        Instant now = clock.instant();
        Instant cooldownThreshold = now.minus(cooldownSeconds, ChronoUnit.SECONDS);

        if (tokenRepository.existsByUser_IdAndCreatedAtAfter(user.getId(), cooldownThreshold)) {
            return;
        }

        tokenRepository.markAllUnusedTokensAsUsed(user.getId(), now);

        String rawToken = generateToken();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .createdAt(now)
                .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
                .build();

        PasswordResetToken savedToken = tokenRepository.save(resetToken);

        try {
            String resetLink = UriComponentsBuilder.fromUriString(resetBaseUrl)
                    .queryParam("token", rawToken)
                    .build(true)
                    .toUriString();

            emailService.sendHtmlEmail(
                    user.getEmail(),
                    "Đặt lại mật khẩu Device Management",
                    "email/password-reset",
                    Map.of(
                            "name", user.getName(),
                            "resetLink", resetLink,
                            "expirationMinutes", expirationMinutes
                    )
            );
        } catch (RuntimeException exception) {
            savedToken.setUsedAt(now);
            log.warn("Password reset email could not be sent");
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private AppException invalidToken() {
        return new AppException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID);
    }
}
