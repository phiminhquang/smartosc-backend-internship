package com.example.device.controller;

import com.example.device.dto.request.DeviceAssignmentRequest;
import com.example.device.dto.request.ReturnDeviceRequest;
import com.example.device.dto.response.ApiResponse;
import com.example.device.dto.response.DeviceAssignmentResponse;
import com.example.device.dto.response.PageResult;
import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.service.AssignmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<PageResult<DeviceAssignmentResponse>> getMyAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.<PageResult<DeviceAssignmentResponse>>builder()
                .result(assignmentService.getMyAssignments(page, size, sort))
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

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'IT_STAFF')")
    public ApiResponse<PageResult<DeviceAssignmentResponse>> getAssignments(
            @RequestParam(required = false) DeviceAssignmentStatus status,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.<PageResult<DeviceAssignmentResponse>>builder()
                .result(assignmentService.getAssignments(status, userId, deviceId, page, size, sort))
                .build();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'IT_STAFF')")
    public ApiResponse<PageResult<DeviceAssignmentResponse>> getAssignmentsByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.<PageResult<DeviceAssignmentResponse>>builder()
                .result(assignmentService.getAssignmentsByUser(userId, page, size, sort))
                .build();
    }
}
