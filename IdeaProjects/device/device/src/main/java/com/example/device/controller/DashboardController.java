package com.example.device.controller;

import com.example.device.dto.response.ApiResponse;
import com.example.device.dto.response.DashboardResponse;
import com.example.device.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'IT_STAFF')")
@Tag(name = "Dashboard", description = "Thống kê hệ thống")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/statistics")
    public ApiResponse<DashboardResponse> getStatistics() {
        return ApiResponse.<DashboardResponse>builder()
                .result(dashboardService.getStatistics())
                .build();
    }
}