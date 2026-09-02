package com.example.device.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private long totalDevices;
    private long availableDevices;
    private long assignedDevices;
    private long underRepairDevices;

    private long totalAssignments;
    private long activeAssignments;
    private long overdueAssignments;
    private long returnedAssignments;

    private long totalRepairs;
    private long pendingRepairs;
    private long inProgressRepairs;
    private long completedRepairs;
    private long unrepairableRepairs;

    private long totalUsers;
    private long employees;
    private long itStaff;
    private long admins;
}