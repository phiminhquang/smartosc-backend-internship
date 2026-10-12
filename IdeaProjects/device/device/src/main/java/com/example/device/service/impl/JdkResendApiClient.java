package com.example.device.service.impl;

import com.example.device.service.ResendApiClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@ConditionalOnProperty(name = "app.mail.transport", havingValue = "resend")
public class JdkResendApiClient implements ResendApiClient {

    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);

    private final HttpClient httpClient;

    public JdkResendApiClient() {
        this(HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build());
    }

    JdkResendApiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public int send(URI apiUri, String apiKey, String idempotencyKey, byte[] requestBody) {
        HttpRequest request = buildRequest(apiUri, apiKey, idempotencyKey, requestBody);
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Resend API call was interrupted", e);
        } catch (IOException e) {
            throw new IllegalStateException("Resend API is temporarily unreachable", e);
        }
    }

    static HttpRequest buildRequest(
            URI apiUri,
            String apiKey,
            String idempotencyKey,
            byte[] requestBody
    ) {
        return HttpRequest.newBuilder(apiUri)
                .timeout(REQUEST_TIMEOUT)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", idempotencyKey)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                .build();
    }
}
