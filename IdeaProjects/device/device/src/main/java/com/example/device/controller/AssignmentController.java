package com.example.device.controller;

import com.example.device.dto.request.DeviceAssignmentRequest;
import com.example.device.dto.request.ReturnDeviceRequest;
import com.example.device.dto.response.ApiResponse;
import com.example.device.dto.response.DeviceAssignmentResponse;
import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Tag(name = "Assignments", description = "Cấp và trả thiết bị")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'IT_STAFF')")
    public ApiResponse<DeviceAssignmentResponse> assignDevice(
            @Valid @RequestBody DeviceAssignmentRequest request) {
        return ApiResponse.<DeviceAssignmentResponse>builder()
                .result(assignmentService.assignDevice(request))
                .build();
    }

    @Operation(summary = "Trả thiết bị và ghi nhận tình trạng")
    @PatchMapping("/{assignmentId}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'IT_STAFF')")
    public ApiResponse<DeviceAssignmentResponse> returnDevice(
            @PathVariable UUID assignmentId,
            @Valid @RequestBody ReturnDeviceRequest request) {
        return ApiResponse.<DeviceAssignmentResponse>builder()
                .result(assignmentService.returnDevice(assignmentId, request))
                .build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ApiResponse<List<DeviceAssignmentResponse>> getMyAssignments() {
        return ApiResponse.<List<DeviceAssignmentResponse>>builder()
                .result(assignmentService.getMyAssignments())
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'IT_STAFF')")
    public ApiResponse<List<DeviceAssignmentResponse>> getAssignments() {
        return ApiResponse.<List<DeviceAssignmentResponse>>builder()
                .result(assignmentService.getAssignments())
                .build();
    }

    @GetMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'IT_STAFF')")
    public ApiResponse<DeviceAssignmentResponse> getAssignment(
            @PathVariable UUID assignmentId) {
        return ApiResponse.<DeviceAssignmentResponse>builder()
                .result(assignmentService.getAssignment(assignmentId))
                .build();
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'IT_STAFF')")
    public ApiResponse<List<DeviceAssignmentResponse>> getAssignmentsByStatus(
            @PathVariable DeviceAssignmentStatus status) {
        return ApiResponse.<List<DeviceAssignmentResponse>>builder()
                .result(assignmentService.getAssignmentsByStatus(status))
                .build();
    }

    @GetMapping("/user/{userId}/devices")
    @PreAuthorize("hasAnyRole('ADMIN', 'IT_STAFF')")
    public ApiResponse<List<DeviceAssignmentResponse>> getDevicesByUser(
            @PathVariable UUID userId) {
        return ApiResponse.<List<DeviceAssignmentResponse>>builder()
                .result(assignmentService.getDevicesByUser(userId))
                .build();
    }
}