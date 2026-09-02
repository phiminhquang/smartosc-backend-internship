package com.example.device.service;

import com.example.device.dto.request.DeviceAssignmentRequest;
import com.example.device.dto.request.ReturnDeviceRequest;
import com.example.device.dto.response.DeviceAssignmentResponse;
import com.example.device.enums.DeviceAssignmentStatus;

import java.util.List;
import java.util.UUID;

public interface AssignmentService {

    DeviceAssignmentResponse assignDevice(DeviceAssignmentRequest request);

    DeviceAssignmentResponse returnDevice(UUID assignmentId, ReturnDeviceRequest request);

    DeviceAssignmentResponse getAssignment(UUID assignmentId);

    List<DeviceAssignmentResponse> getAssignments();

    List<DeviceAssignmentResponse> getDevicesByUser(UUID userId);

    List<DeviceAssignmentResponse> getAssignmentsByStatus(DeviceAssignmentStatus status);

    List<DeviceAssignmentResponse> getMyAssignments();

    int updateOverdueAssignments();

    int sendUpcomingDueNotifications();

    int sendOverdueNotifications();

    int sendDailyOverdueSummary();

}