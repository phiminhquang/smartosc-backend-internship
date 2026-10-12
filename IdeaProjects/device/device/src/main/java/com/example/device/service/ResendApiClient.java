package com.example.device.service;

import java.net.URI;

public interface ResendApiClient {

    int send(URI apiUri, String apiKey, String idempotencyKey, byte[] requestBody);
}
