package com.example.device.controller;

import com.example.device.dto.request.DeviceCreationRequest;
import com.example.device.dto.request.DeviceStatusUpdateRequest;
import com.example.device.dto.request.DeviceUpdateRequest;

import com.example.device.dto.response.DeviceResponse;
import com.example.device.enums.DeviceCategory;
import com.example.device.enums.DeviceState;
import com.example.device.service.DeviceFileService;
import com.example.device.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.device.dto.response.ApiResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@PreAuthorize("hasAnyRole('ADMIN', 'IT_STAFF')")
@RequiredArgsConstructor
@Tag(name = "Devices", description = "Quản lý thiết bị")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceFileService deviceFileService;

    @PostMapping
    public ApiResponse<DeviceResponse> createDevice(
            @Valid @RequestBody DeviceCreationRequest request
    ) {
        return ApiResponse.<DeviceResponse>builder()
                .result(deviceService.createDevice(request))
                .build();
    }

    @Operation(summary = "Tìm kiếm và phân trang thiết bị")
    @GetMapping
    public ApiResponse<Page<DeviceResponse>> getDevices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) DeviceState state,
            @RequestParam(required = false) DeviceCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.<Page<DeviceResponse>>builder()
                .result(deviceService.searchDevices(keyword, state, category, page, size))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<DeviceResponse> getDeviceById(
            @PathVariable UUID id
    ) {
        return ApiResponse.<DeviceResponse>builder()
                .result(deviceService.getDeviceById(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<DeviceResponse> updateDevice(
            @PathVariable UUID id,
            @Valid @RequestBody DeviceUpdateRequest request
    ) {
        return ApiResponse.<DeviceResponse>builder()
                .result(deviceService.updateDevice(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDevice(
            @PathVariable UUID id
    ) {
        deviceService.deleteDevice(id);

        return ApiResponse.<Void>builder()
                .message("Xóa thiết bị thành công")
                .build();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<DeviceResponse> updateDeviceState(
            @PathVariable UUID id,
            @Valid @RequestBody DeviceStatusUpdateRequest request
    ) {
        return ApiResponse.<DeviceResponse>builder()
                .result(deviceService.updateDeviceState(id, request))
                .build();
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"devices.csv\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(deviceFileService.exportCsv());
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"devices.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(deviceFileService.exportExcel());
    }

    @PostMapping(value = "/import/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Integer> importCsv(@RequestParam("file") MultipartFile file) {
        return ApiResponse.<Integer>builder()
                .message("Import thiết bị thành công")
                .result(deviceFileService.importCsv(file))
                .build();
    }

}