package com.example.device.service.impl;

import com.example.device.dto.request.PasswordResetConfirmRequest;
import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import com.example.device.model.PasswordResetToken;
import com.example.device.model.User;
import com.example.device.repository.PasswordResetTokenRepository;
import com.example.device.repository.UserRepository;
import com.example.device.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-09-28T10:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private Clock clock;

    private PasswordResetServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetServiceImpl(
                userRepository,
                tokenRepository,
                passwordEncoder,
                emailService,
                clock
        );
        ReflectionTestUtils.setField(service, "resetBaseUrl", "http://localhost:5173/reset-password");
        ReflectionTestUtils.setField(service, "expirationMinutes", 15L);
        ReflectionTestUtils.setField(service, "cooldownSeconds", 60L);
        lenient().when(clock.instant()).thenReturn(NOW);
    }

    @Test
    void requestReset_existingUser_shouldStoreOnlyHashAndEmailRawToken() {
        User user = user();
        when(userRepository.findByEmailForUpdate(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.existsByUser_IdAndCreatedAtAfter(eq(user.getId()), any()))
                .thenReturn(false);
        when(tokenRepository.save(any())).thenAnswer(invocation -> {
            PasswordResetToken token = invocation.getArgument(0);
            token.setId(UUID.randomUUID());
            return token;
        });

        service.requestReset(user.getEmail());

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken storedToken = tokenCaptor.getValue();

        assertEquals(64, storedToken.getTokenHash().length());
        assertEquals(NOW, storedToken.getCreatedAt());
        assertEquals(NOW.plusSeconds(15 * 60), storedToken.getExpiresAt());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> variablesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendHtmlEmail(
                eq(user.getEmail()),
                anyString(),
                eq("email/password-reset"),
                variablesCaptor.capture()
        );

        String resetLink = (String) variablesCaptor.getValue().get("resetLink");
        String rawToken = resetLink.substring(resetLink.indexOf("token=") + "token=".length());
        assertFalse(rawToken.isBlank());
        assertNotEquals(rawToken, storedToken.getTokenHash());
    }

    @Test
    void requestReset_unknownEmail_shouldReturnWithoutCreatingToken() {
        when(userRepository.findByEmailForUpdate("unknown@example.com")).thenReturn(Optional.empty());

        service.requestReset("unknown@example.com");

        verifyNoInteractions(tokenRepository, emailService);
    }

    @Test
    void requestReset_duringCooldown_shouldNotCreateOrSendAgain() {
        User user = user();
        when(userRepository.findByEmailForUpdate(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.existsByUser_IdAndCreatedAtAfter(eq(user.getId()), any()))
                .thenReturn(true);

        service.requestReset(user.getEmail());

        verify(tokenRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void confirmReset_validToken_shouldEncodePasswordConsumeTokenAndRevokeJwt() {
        User user = user();
        user.setTokenVersion(4);
        PasswordResetToken token = validToken(user);
        when(tokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("new-password")).thenReturn("bcrypt-hash");

        service.confirmReset(confirmRequest("raw-token", "new-password"));

        assertEquals("bcrypt-hash", user.getPassword());
        assertEquals(5, user.getTokenVersion());
        assertEquals(NOW, token.getUsedAt());
        verify(tokenRepository).findByTokenHashForUpdate(argThat(hash ->
                hash.length() == 64 && !hash.equals("raw-token")
        ));
        verify(tokenRepository).markOtherUnusedTokensAsUsed(user.getId(), token.getId(), NOW);
    }

    @Test
    void confirmReset_invalidToken_shouldUseGenericError() {
        when(tokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> service.confirmReset(confirmRequest("invalid", "new-password"))
        );

        assertSame(ErrorCode.PASSWORD_RESET_TOKEN_INVALID, exception.getErrorCode());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void confirmReset_expiredToken_shouldUseGenericError() {
        PasswordResetToken token = validToken(user());
        token.setExpiresAt(NOW);
        when(tokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(token));

        AppException exception = assertThrows(
                AppException.class,
                () -> service.confirmReset(confirmRequest("expired", "new-password"))
        );

        assertSame(ErrorCode.PASSWORD_RESET_TOKEN_INVALID, exception.getErrorCode());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void confirmReset_usedTokenCannotBeUsedTwice() {
        PasswordResetToken token = validToken(user());
        when(tokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode(anyString())).thenReturn("bcrypt-hash");
        PasswordResetConfirmRequest request = confirmRequest("single-use", "new-password");

        service.confirmReset(request);

        AppException exception = assertThrows(AppException.class, () -> service.confirmReset(request));
        assertSame(ErrorCode.PASSWORD_RESET_TOKEN_INVALID, exception.getErrorCode());
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    private User user() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .name("Test User")
                .password("old-hash")
                .build();
    }

    private PasswordResetToken validToken(User user) {
        return PasswordResetToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash("a".repeat(64))
                .createdAt(NOW.minusSeconds(30))
                .expiresAt(NOW.plusSeconds(300))
                .build();
    }

    private PasswordResetConfirmRequest confirmRequest(String token, String password) {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        request.setToken(token);
        request.setNewPassword(password);
        return request;
    }
}
