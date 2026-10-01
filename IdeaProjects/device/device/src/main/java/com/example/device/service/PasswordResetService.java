package com.example.device.service;

import com.example.device.dto.request.PasswordResetConfirmRequest;

public interface PasswordResetService {

    void requestReset(String email);

    void confirmReset(PasswordResetConfirmRequest request);
}
