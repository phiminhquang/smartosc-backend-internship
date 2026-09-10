package com.example.device.service.impl;

import com.example.device.dto.response.DashboardResponse;
import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.enums.DeviceState;
import com.example.device.enums.RepairStatus;
import com.example.device.repository.DeviceAssignmentRepository;
import com.example.device.repository.DeviceRepairRepository;
import com.example.device.repository.DeviceRepository;
import com.example.device.repository.UserRepository;
import com.example.device.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DeviceRepository deviceRepository;
    private final DeviceAssignmentRepository assignmentRepository;
    private final DeviceRepairRepository repairRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getStatistics() {

        DeviceRepository.DeviceStatistics deviceStats =
                deviceRepository.getStatistics(
                        DeviceState.AVAILABLE,
                        DeviceState.ASSIGNED,
                        DeviceState.UNDER_REPAIR
                );

        DeviceAssignmentRepository.AssignmentStatistics assignmentStats =
                assignmentRepository.getStatistics(
                        DeviceAssignmentStatus.ACTIVE,
                        DeviceAssignmentStatus.OVERDUE,
                        DeviceAssignmentStatus.RETURNED
                );

        DeviceRepairRepository.RepairStatistics repairStats =
                repairRepository.getStatistics(
                        RepairStatus.PENDING,
                        RepairStatus.IN_PROGRESS,
                        RepairStatus.COMPLETED,
                        RepairStatus.UNREPAIRABLE
                );

        UserRepository.UserStatistics userStats =
                userRepository.getStatistics(
                        "EMPLOYEE",
                        "IT_STAFF",
                        "ADMIN"
                );

        return DashboardResponse.builder()
                .totalDevices(deviceStats.getTotalDevices())
                .availableDevices(deviceStats.getAvailableDevices())
                .assignedDevices(deviceStats.getAssignedDevices())
                .underRepairDevices(deviceStats.getUnderRepairDevices())

                .totalAssignments(assignmentStats.getTotalAssignments())
                .activeAssignments(assignmentStats.getActiveAssignments())
                .overdueAssignments(assignmentStats.getOverdueAssignments())
                .returnedAssignments(assignmentStats.getReturnedAssignments())

                .totalRepairs(repairStats.getTotalRepairs())
                .pendingRepairs(repairStats.getPendingRepairs())
                .inProgressRepairs(repairStats.getInProgressRepairs())
                .completedRepairs(repairStats.getCompletedRepairs())
                .unrepairableRepairs(repairStats.getUnrepairableRepairs())

                .totalUsers(userStats.getTotalUsers())
                .employees(userStats.getEmployees())
                .itStaff(userStats.getItStaff())
                .admins(userStats.getAdmins())

                .build();
    }
}