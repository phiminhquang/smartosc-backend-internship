package com.example.device.service;

import java.time.Instant;

public interface RateLimitService {

    void checkLogin(String email, String clientAddress);

    void checkPasswordResetRequest(String email, String clientAddress);

    int cleanupExpired(Instant now, int batchSize);
}
