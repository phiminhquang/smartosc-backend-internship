package com.example.device.service.impl;

import com.example.device.enums.EmailJobType;
import com.example.device.service.EmailJobDelivery;
import com.example.device.service.EmailJobSecretCipher;
import com.example.device.service.EmailTransport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailJobSenderImplTest {

    @Mock
    private EmailTransport emailTransport;
    @Mock
    private TemplateEngine templateEngine;
    @Mock
    private EmailJobSecretCipher secretCipher;

    private EmailJobSenderImpl sender;

    @BeforeEach
    void setUp() {
        sender = new EmailJobSenderImpl(
                emailTransport,
                templateEngine,
                JsonMapper.builder().build(),
                secretCipher
        );
        ReflectionTestUtils.setField(sender, "resetBaseUrl", "http://device.test/reset-password");
    }

    @Test
    void rendersAndDelegatesDeliveryToConfiguredTransport() {
        UUID jobId = UUID.fromString("6ff2fe33-553d-48c3-8023-78bce074f815");
        when(templateEngine.process(eq("email/device-reminder"), any())).thenReturn("<p>Reminder</p>");

        sender.send(new EmailJobDelivery(
                jobId,
                "request-123",
                EmailJobType.ASSIGNMENT_REMINDER,
                "recipient@device.test",
                "Reminder",
                "email/device-reminder",
                1,
                "{}",
                null,
                null,
                null,
                null,
                null,
                1,
                5
        ));

        verify(emailTransport).send(
                jobId,
                "recipient@device.test",
                "Reminder",
                "<p>Reminder</p>"
        );
    }

    @Test
    void passwordResetDecryptsSecretOnlyWhenPreparingTemplate() {
        UUID jobId = UUID.fromString("96db8c6d-c2e5-42c5-bd47-7c807e302c8f");
        byte[] ciphertext = new byte[]{1, 2, 3};
        byte[] nonce = new byte[12];
        when(secretCipher.decrypt(EmailJobType.PASSWORD_RESET, ciphertext, nonce, "test-v1"))
                .thenReturn("url-safe-raw-token");
        when(templateEngine.process(eq("email/password-reset"), any())).thenReturn("<p>Reset</p>");

        sender.send(new EmailJobDelivery(
                jobId,
                "request-456",
                EmailJobType.PASSWORD_RESET,
                "recipient@device.test",
                "Password reset",
                "email/password-reset",
                1,
                "{\"name\":\"Test User\",\"expirationMinutes\":15}",
                ciphertext,
                nonce,
                "test-v1",
                UUID.randomUUID(),
                java.time.Instant.parse("2026-10-10T01:00:00Z"),
                1,
                4
        ));

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("email/password-reset"), contextCaptor.capture());
        assertEquals("Test User", contextCaptor.getValue().getVariable("name"));
        assertEquals(15, contextCaptor.getValue().getVariable("expirationMinutes"));
        assertEquals(
                "http://device.test/reset-password?token=url-safe-raw-token",
                contextCaptor.getValue().getVariable("resetLink")
        );
        verify(emailTransport).send(
                jobId,
                "recipient@device.test",
                "Password reset",
                "<p>Reset</p>"
        );
    }
}
