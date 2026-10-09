package com.example.device.controller;

import com.example.device.dto.request.UserCreationRequest;
import com.example.device.dto.request.UserUpdateRequest;
import com.example.device.dto.response.ApiResponse;
import com.example.device.dto.response.PageResult;
import com.example.device.dto.response.UserCreationResponse;
import com.example.device.service.AssignmentService;
import com.example.device.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AssignmentService assignmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserCreationResponse> createUser(
            @Valid @RequestBody UserCreationRequest request) {
        return ApiResponse.<UserCreationResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResult<UserCreationResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.<PageResult<UserCreationResponse>>builder()
                .result(userService.getUsers(keyword, role, page, size, sort))
                .build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserCreationResponse> getCurrentUser() {
        return ApiResponse.<UserCreationResponse>builder()
                .result(userService.getCurrentUser())
                .build();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserCreationResponse> getUser(@PathVariable UUID userId) {
        return ApiResponse.<UserCreationResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserCreationResponse> updateUser(
            @PathVariable UUID userId, @Valid @RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserCreationResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);

        return ApiResponse.<Void>builder()
                .message("Xóa user thành công")
                .build();
    }

    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserCreationResponse> assignRole(
            @PathVariable UUID userId, @PathVariable UUID roleId) {
        return ApiResponse.<UserCreationResponse>builder()
                .result(userService.assignRole(userId, roleId))
                .build();
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserCreationResponse> removeRole(
            @PathVariable UUID userId, @PathVariable UUID roleId) {
        return ApiResponse.<UserCreationResponse>builder()
                .result(userService.removeRole(userId, roleId))
                .build();
    }

}
