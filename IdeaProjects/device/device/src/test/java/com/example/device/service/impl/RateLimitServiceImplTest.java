package com.example.device.service.impl;

import com.example.device.configuration.RateLimitProperties;
import com.example.device.exception.ErrorCode;
import com.example.device.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-10-11T10:02:00Z");

    @Mock
    private RateLimitCounterStore counterStore;

    private RateLimitServiceImpl service;

    @BeforeEach
    void setUp() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setLogin(new RateLimitProperties.Policy(20, 5, Duration.ofMinutes(5)));
        properties.setPasswordReset(new RateLimitProperties.Policy(10, 3, Duration.ofMinutes(15)));
        service = new RateLimitServiceImpl(
                counterStore,
                properties,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void passwordResetChecksIpAndNormalizedIdentityWithoutPersistingRawValues() {
        when(counterStore.incrementAndGet(any(), any(), any())).thenReturn(1);

        service.checkPasswordResetRequest("  User@Example.COM ", " 192.0.2.10 ");
        service.checkPasswordResetRequest("user@example.com", "192.0.2.10");

        ArgumentCaptor<byte[]> keyCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(counterStore, times(4)).incrementAndGet(
                keyCaptor.capture(),
                any(Instant.class),
                any(Instant.class)
        );
        List<byte[]> keys = keyCaptor.getAllValues();
        assertEquals(32, keys.get(0).length);
        assertEquals(32, keys.get(1).length);
        assertArrayEquals(keys.get(0), keys.get(2));
        assertArrayEquals(keys.get(1), keys.get(3));
    }

    @Test
    void passwordResetIdentityLimitReturnsGeneric429DecisionAndRetryAfter() {
        when(counterStore.incrementAndGet(any(), any(), any())).thenReturn(1, 4);

        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> service.checkPasswordResetRequest("unknown@example.com", "192.0.2.11")
        );

        assertSame(ErrorCode.RATE_LIMIT_EXCEEDED, exception.getErrorCode());
        assertEquals(780, exception.getRetryAfterSeconds());
        verify(counterStore, times(2)).incrementAndGet(any(), any(), any());
    }

    @Test
    void loginIpLimitStopsBeforeIdentityBucket() {
        when(counterStore.incrementAndGet(any(), any(), any())).thenReturn(21);

        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> service.checkLogin("user@example.com", "192.0.2.12")
        );

        assertTrue(exception.getRetryAfterSeconds() > 0);
        verify(counterStore, times(1)).incrementAndGet(any(), any(), any());
    }
}
