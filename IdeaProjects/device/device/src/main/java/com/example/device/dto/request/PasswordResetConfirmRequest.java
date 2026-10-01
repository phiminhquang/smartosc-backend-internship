package com.example.device.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetConfirmRequest {

    @NotBlank(message = "PASSWORD_RESET_TOKEN_INVALID")
    @Size(min = 32, max = 128, message = "PASSWORD_RESET_TOKEN_INVALID")
    private String token;

    @NotBlank(message = "INVALID_PASSWORD")
    @Size(min = 8, max = 128, message = "INVALID_PASSWORD")
    private String newPassword;
}
