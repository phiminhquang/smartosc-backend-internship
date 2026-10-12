package com.example.device.service.impl;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JdkResendApiClientTest {

    @Test
    void buildsHttpsRequestWithSecretAndIdempotencyHeaders() {
        HttpRequest request = JdkResendApiClient.buildRequest(
                URI.create("https://api.resend.com/emails"),
                "re_test_secret",
                "email-job-123",
                "{}".getBytes(StandardCharsets.UTF_8)
        );

        assertEquals("POST", request.method());
        assertEquals(URI.create("https://api.resend.com/emails"), request.uri());
        assertEquals(JdkResendApiClient.REQUEST_TIMEOUT, request.timeout().orElseThrow());
        assertEquals("Bearer re_test_secret", request.headers().firstValue("Authorization").orElseThrow());
        assertEquals("application/json", request.headers().firstValue("Content-Type").orElseThrow());
        assertEquals("email-job-123", request.headers().firstValue("Idempotency-Key").orElseThrow());
    }
}
