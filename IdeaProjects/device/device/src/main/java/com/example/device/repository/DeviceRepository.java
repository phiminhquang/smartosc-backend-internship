package com.example.device.repository;

import com.example.device.enums.DeviceState;
import com.example.device.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID>, JpaSpecificationExecutor<Device> {

    boolean existsBySerialNumber(String serialNumber);

    long countByState(DeviceState state);
}