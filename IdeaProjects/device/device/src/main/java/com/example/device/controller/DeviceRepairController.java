package com.example.device.controller;

import com.example.device.dto.request.RepairCompleteRequest;
import com.example.device.dto.request.RepairCreationRequest;
import com.example.device.dto.request.RepairUnrepairableRequest;
import com.example.device.dto.response.ApiResponse;
import com.example.device.dto.response.RepairResponse;
import com.example.device.dto.response.PageResult;
import com.example.device.enums.RepairStatus;
import com.example.device.service.DeviceRepairService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/repairs")
@PreAuthorize("hasAnyRole('ADMIN', 'IT_STAFF')")
@RequiredArgsConstructor
@Tag(name = "Repairs", description = "Quản lý sửa chữa thiết bị")
public class DeviceRepairController {

    private final DeviceRepairService repairService;

    @PostMapping
    public ApiResponse<RepairResponse> createRepair(
            @Valid @RequestBody RepairCreationRequest request) {
        return ApiResponse.<RepairResponse>builder()
                .result(repairService.createRepair(request))
                .build();
    }

    @PatchMapping("/{repairId}/start")
    public ApiResponse<RepairResponse> startRepair(@PathVariable UUID repairId) {
        return ApiResponse.<RepairResponse>builder()
                .result(repairService.startRepair(repairId))
                .build();
    }

    @Operation(summary = "Hoàn thành sửa chữa thiết bị")
    @PatchMapping("/{repairId}/complete")
    public ApiResponse<RepairResponse> completeRepair(
            @PathVariable UUID repairId,
            @Valid @RequestBody RepairCompleteRequest request) {
        return ApiResponse.<RepairResponse>builder()
                .result(repairService.completeRepair(repairId, request))
                .build();
    }

    @PatchMapping("/{repairId}/unrepairable")
    public ApiResponse<RepairResponse> markUnrepairable(
            @PathVariable UUID repairId,
            @Valid @RequestBody RepairUnrepairableRequest request) {
        return ApiResponse.<RepairResponse>builder()
                .result(repairService.markUnrepairable(repairId, request))
                .build();
    }

    @GetMapping
    public ApiResponse<PageResult<RepairResponse>> getRepairs(
            @RequestParam(required = false) RepairStatus status,
            @RequestParam(required = false) UUID deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.<PageResult<RepairResponse>>builder()
                .result(repairService.getRepairs(status, deviceId, page, size, sort))
                .build();
    }

    @GetMapping("/{repairId}")
    public ApiResponse<RepairResponse> getRepair(@PathVariable UUID repairId) {
        return ApiResponse.<RepairResponse>builder()
                .result(repairService.getRepair(repairId))
                .build();
    }

    @GetMapping("/device/{deviceId}")
    public ApiResponse<PageResult<RepairResponse>> getRepairsByDevice(
            @PathVariable UUID deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.<PageResult<RepairResponse>>builder()
                .result(repairService.getRepairsByDevice(deviceId, page, size, sort))
                .build();
    }
}
