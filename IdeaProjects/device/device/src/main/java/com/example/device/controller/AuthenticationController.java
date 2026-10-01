package com.example.device.controller;

import com.example.device.dto.request.AuthenticationRequest;
import com.example.device.dto.request.PasswordResetConfirmRequest;
import com.example.device.dto.request.PasswordResetRequest;
import com.example.device.dto.request.IntrospectRequest;
import com.example.device.dto.response.AuthenticationResponse;
import com.example.device.dto.response.IntrospectResponse;
import com.example.device.service.AuthenticationService;
import com.example.device.service.PasswordResetService;
import com.example.device.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/token")
    public ApiResponse<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        return ApiResponse.<AuthenticationResponse>builder()
                .result(authenticationService.authenticate(request))
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(
            @Valid @RequestBody IntrospectRequest request
    ) {
        return ApiResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspect(request))
                .build();
    }

    @PostMapping("/password-reset/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<Void> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        passwordResetService.requestReset(request.getEmail());

        return ApiResponse.<Void>builder()
                .message("Nếu email tồn tại, hướng dẫn đặt lại mật khẩu đã được gửi")
                .build();
    }

    @PostMapping("/password-reset/confirm")
    public ApiResponse<Void> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request
    ) {
        passwordResetService.confirmReset(request);

        return ApiResponse.<Void>builder()
                .message("Đặt lại mật khẩu thành công")
                .build();
    }
}
