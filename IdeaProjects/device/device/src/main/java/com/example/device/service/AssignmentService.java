package com.example.device.service;

import com.example.device.dto.request.DeviceAssignmentRequest;
import com.example.device.dto.request.ReturnDeviceRequest;
import com.example.device.dto.response.DeviceAssignmentResponse;
import com.example.device.dto.response.PageResult;
import com.example.device.enums.DeviceAssignmentStatus;

import java.util.UUID;

public interface AssignmentService {

    DeviceAssignmentResponse assignDevice(DeviceAssignmentRequest request);

    DeviceAssignmentResponse returnDevice(UUID assignmentId, ReturnDeviceRequest request);

    DeviceAssignmentResponse getAssignment(UUID assignmentId);

    PageResult<DeviceAssignmentResponse> getAssignments(
            DeviceAssignmentStatus status,
            UUID userId,
            UUID deviceId,
            int page,
            int size,
            String sort
    );

    PageResult<DeviceAssignmentResponse> getAssignmentsByUser(
            UUID userId,
            int page,
            int size,
            String sort
    );

    PageResult<DeviceAssignmentResponse> getMyAssignments(
            int page,
            int size,
            String sort
    );

    int updateOverdueAssignments();

    int sendUpcomingDueNotifications();

    int sendOverdueNotifications();

    int sendDailyOverdueSummary();

}
