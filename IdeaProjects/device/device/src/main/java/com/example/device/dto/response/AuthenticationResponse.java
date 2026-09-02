package com.example.device.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder

public class AuthenticationResponse {
    private boolean authenticated;
    private String token;
    private Instant expiryTime;
}
