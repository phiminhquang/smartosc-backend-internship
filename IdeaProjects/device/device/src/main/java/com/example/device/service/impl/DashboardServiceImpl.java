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
        return DashboardResponse.builder()
                .totalDevices(deviceRepository.count())
                .availableDevices(deviceRepository.countByState(DeviceState.AVAILABLE))
                .assignedDevices(deviceRepository.countByState(DeviceState.ASSIGNED))
                .underRepairDevices(deviceRepository.countByState(DeviceState.UNDER_REPAIR))

                .totalAssignments(assignmentRepository.count())
                .activeAssignments(assignmentRepository.countByStatus(DeviceAssignmentStatus.ACTIVE))
                .overdueAssignments(assignmentRepository.countByStatus(DeviceAssignmentStatus.OVERDUE))
                .returnedAssignments(assignmentRepository.countByStatus(DeviceAssignmentStatus.RETURNED))

                .totalRepairs(repairRepository.count())
                .pendingRepairs(repairRepository.countByStatus(RepairStatus.PENDING))
                .inProgressRepairs(repairRepository.countByStatus(RepairStatus.IN_PROGRESS))
                .completedRepairs(repairRepository.countByStatus(RepairStatus.COMPLETED))
                .unrepairableRepairs(repairRepository.countByStatus(RepairStatus.UNREPAIRABLE))

                .totalUsers(userRepository.count())
                .employees(userRepository.countDistinctByRoles_Name("EMPLOYEE"))
                .itStaff(userRepository.countDistinctByRoles_Name("IT_STAFF"))
                .admins(userRepository.countDistinctByRoles_Name("ADMIN"))
                .build();
    }
}