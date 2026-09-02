package com.example.device.repository;

import com.example.device.enums.RepairStatus;
import com.example.device.model.DeviceRepair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceRepairRepository extends JpaRepository<DeviceRepair, UUID> {

    boolean existsByDeviceId(UUID deviceId);
    boolean existsByDeviceIdAndStatusIn(UUID deviceId, List<RepairStatus> statuses);

    List<DeviceRepair> findAllByOrderByCreatedAtDesc();
    List<DeviceRepair> findByDeviceIdOrderByCreatedAtDesc(UUID deviceId);

    long countByStatus(RepairStatus status);
}