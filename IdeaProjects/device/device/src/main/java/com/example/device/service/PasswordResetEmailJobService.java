package com.example.device.service;

import com.example.device.model.PasswordResetToken;
import com.example.device.model.User;

public interface PasswordResetEmailJobService {

    void enqueue(
            User user,
            PasswordResetToken resetToken,
            String rawToken,
            long expirationMinutes
    );
}
