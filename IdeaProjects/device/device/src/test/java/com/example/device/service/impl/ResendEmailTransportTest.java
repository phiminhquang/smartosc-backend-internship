package com.example.device.service.impl;

import com.example.device.service.EmailJobPermanentException;
import com.example.device.service.ResendApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResendEmailTransportTest {

    private final RecordingApiClient apiClient = new RecordingApiClient();
    private ResendEmailTransport transport;

    @BeforeEach
    void setUp() {
        transport = new ResendEmailTransport(
                apiClient,
                JsonMapper.builder().build(),
                "https://api.resend.test/emails",
                "re_test_secret",
                "Device Management <onboarding@resend.dev>",
                "owner@example.test"
        );
    }

    @Test
    void sendsJsonWithSecretAndStableIdempotencyKeyThroughApiClient() {
        UUID jobId = UUID.fromString("6ff2fe33-553d-48c3-8023-78bce074f815");

        transport.send(jobId, "OWNER@example.test", "Reset", "<p>Reset link</p>");

        assertEquals(1, apiClient.requestCount);
        assertEquals(URI.create("https://api.resend.test/emails"), apiClient.apiUri);
        assertEquals("re_test_secret", apiClient.apiKey);
        assertEquals("email-job-" + jobId, apiClient.idempotencyKey);
        String body = new String(apiClient.requestBody, StandardCharsets.UTF_8);
        assertTrue(body.contains("\"from\":\"Device Management <onboarding@resend.dev>\""));
        assertTrue(body.contains("\"to\":[\"OWNER@example.test\"]"));
        assertTrue(body.contains("\"subject\":\"Reset\""));
        assertTrue(body.contains("\"html\":\"<p>Reset link</p>\""));
    }

    @Test
    void rejectsRecipientOutsideDemoAllowListWithoutCallingProvider() {
        assertThrows(
                EmailJobPermanentException.class,
                () -> transport.send(UUID.randomUUID(), "other@example.test", "Reset", "<p>Reset</p>")
        );
        assertEquals(0, apiClient.requestCount);
    }

    @Test
    void treatsProviderValidationFailureAsPermanent() {
        apiClient.statusCode = 400;
        assertThrows(
                EmailJobPermanentException.class,
                () -> transport.send(UUID.randomUUID(), "owner@example.test", "Reset", "<p>Reset</p>")
        );
    }

    @Test
    void treatsRateLimitAsRetryable() {
        apiClient.statusCode = 429;
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> transport.send(UUID.randomUUID(), "owner@example.test", "Reset", "<p>Reset</p>")
        );
        assertTrue(!(exception instanceof EmailJobPermanentException));
    }

    private static final class RecordingApiClient implements ResendApiClient {

        private int statusCode = 200;
        private int requestCount;
        private URI apiUri;
        private String apiKey;
        private String idempotencyKey;
        private byte[] requestBody;

        @Override
        public int send(URI apiUri, String apiKey, String idempotencyKey, byte[] requestBody) {
            requestCount++;
            this.apiUri = apiUri;
            this.apiKey = apiKey;
            this.idempotencyKey = idempotencyKey;
            this.requestBody = requestBody;
            return statusCode;
        }
    }
}
