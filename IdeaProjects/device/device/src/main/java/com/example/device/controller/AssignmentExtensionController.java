package com.example.device.controller;

import com.example.device.dto.request.ExtensionRequestCreationRequest;
import com.example.device.dto.request.ExtensionReviewRequest;
import com.example.device.dto.response.ApiResponse;
import com.example.device.dto.response.ExtensionResponse;
import com.example.device.dto.response.PageResult;
import com.example.device.enums.ExtensionRequestStatus;
import com.example.device.service.AssignmentExtensionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/extension-requests")
@RequiredArgsConstructor
@Tag(name = "Extensions", description = "Quản lý yêu cầu gia hạn")
public class AssignmentExtensionController {

    private final AssignmentExtensionService extensionService;

    @PostMapping("/assignments/{assignmentId}/extension-requests")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ApiResponse<ExtensionResponse> createRequest(
            @PathVariable UUID assignmentId,
            @Valid @RequestBody ExtensionRequestCreationRequest request) {

        return ApiResponse.<ExtensionResponse>builder()
                .result(extensionService.createRequest(assignmentId, request))
                .build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ApiResponse<PageResult<ExtensionResponse>> getMyRequests(
            @RequestParam(required = false) ExtensionRequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.<PageResult<ExtensionResponse>>builder()
                .result(extensionService.getMyRequests(status, page, size, sort))
                .build();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResult<ExtensionResponse>> getPendingRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.<PageResult<ExtensionResponse>>builder()
                .result(extensionService.getPendingRequests(page, size, sort))
                .build();
    }

    @PatchMapping("/{requestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ExtensionResponse> approveRequest(
            @PathVariable UUID requestId,
            @RequestBody(required = false) ExtensionReviewRequest request) {
        return ApiResponse.<ExtensionResponse>builder()
                .result(extensionService.approveRequest(requestId, request))
                .build();
    }

    @PatchMapping("/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ExtensionResponse> rejectRequest(
            @PathVariable UUID requestId,
            @RequestBody(required = false) ExtensionReviewRequest request) {
        return ApiResponse.<ExtensionResponse>builder()
                .result(extensionService.rejectRequest(requestId, request))
                .build();
    }
}
