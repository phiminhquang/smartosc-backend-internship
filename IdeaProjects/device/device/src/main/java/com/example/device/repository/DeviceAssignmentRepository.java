package com.example.device.repository;

import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.model.DeviceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DeviceAssignmentRepository extends JpaRepository<DeviceAssignment, UUID> {

    boolean existsByDeviceId(UUID deviceId);

    boolean existsByUserId(UUID userId);

    boolean existsByDeviceIdAndStatusIn(UUID deviceId, List<DeviceAssignmentStatus> statuses);

    List<DeviceAssignment> findAllByOrderByAssignedAtDesc();

    List<DeviceAssignment> findByUserIdOrderByAssignedAtDesc(UUID userId);

    List<DeviceAssignment> findByStatusOrderByAssignedAtDesc(DeviceAssignmentStatus status);

    List<DeviceAssignment> findByStatusAndExpectedReturnAtBefore(DeviceAssignmentStatus status, LocalDateTime time  );

    List<DeviceAssignment> findByStatusAndOverdueNotifiedAtIsNull(DeviceAssignmentStatus status);

    List<DeviceAssignment> findByStatusAndReminderNotifiedAtIsNullAndExpectedReturnAtAfterAndExpectedReturnAtBefore(
            DeviceAssignmentStatus status, LocalDateTime start, LocalDateTime end);

    List<DeviceAssignment> findByStatusOrderByExpectedReturnAtAsc(DeviceAssignmentStatus status);

    List<DeviceAssignment> findByUser_EmailOrderByAssignedAtDesc(String email);

    long countByStatus(DeviceAssignmentStatus status);

}