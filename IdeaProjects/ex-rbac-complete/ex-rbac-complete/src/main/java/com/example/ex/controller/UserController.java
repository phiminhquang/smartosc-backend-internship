package com.example.ex.controller;

import com.example.ex.dto.request.UserCreationRequest;
import com.example.ex.dto.response.ApiResponse;
import com.example.ex.dto.response.UserResponse;
import com.example.ex.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    // Đăng ký tài khoản
    // Public nên không cần @PreAuthorize
    @PostMapping
    public ApiResponse<UserResponse> createUser(
            @Valid @RequestBody UserCreationRequest request
    ) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }


    // ADMIN hoặc EDITOR được xem danh sách user
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }


    // ADMIN hoặc EDITOR được xem một user
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ApiResponse<UserResponse> getUser(
            @PathVariable UUID userId
    ) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }


    // Chỉ ADMIN được gán role
    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> assignRole(
            @PathVariable UUID userId,
            @PathVariable Integer roleId
    ) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.assignRole(userId, roleId))
                .build();
    }


    // Chỉ cần đăng nhập là dùng được
    @GetMapping("/me")
    public String getCurrentUser(Authentication authentication) {
        return authentication.getName();
    }
}