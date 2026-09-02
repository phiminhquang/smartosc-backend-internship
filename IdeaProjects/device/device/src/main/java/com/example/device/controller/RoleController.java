package com.example.device.controller;

import com.example.device.dto.request.RoleCreationRequest;
import com.example.device.dto.request.RoleUpdateRequest;

import com.example.device.dto.response.RoleResponse;
import com.example.device.service.RoleService;
import com.example.device.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<RoleResponse>> getRoles() {
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getRoles())
                .build();
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoleResponse> getRoleById(@PathVariable UUID roleId) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.getRoleById(roleId))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody RoleCreationRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.createRole(request))
                .build();
    }

    @PutMapping("/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoleResponse> updateRole(@PathVariable UUID roleId, @Valid @RequestBody RoleUpdateRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.updateRole(roleId, request))
                .build();
    }

    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteRole(@PathVariable UUID roleId) {
        roleService.deleteRole(roleId);

        return ApiResponse.<Void>builder()
                .message("Xóa role thành công")
                .build();
    }
}