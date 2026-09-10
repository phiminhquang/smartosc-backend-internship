package com.example.device.repository;

import com.example.device.enums.DeviceState;
import com.example.device.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID>, JpaSpecificationExecutor<Device> {

    boolean existsBySerialNumber(String serialNumber);

    @Query("""
    select
        count(d) as totalDevices,
        coalesce(
            sum(case when d.state = :available then 1 else 0 end),
            0
        ) as availableDevices,
        coalesce(
            sum(case when d.state = :assigned then 1 else 0 end),
            0
        ) as assignedDevices,
        coalesce(
            sum(case when d.state = :underRepair then 1 else 0 end),
            0
        ) as underRepairDevices
    from Device d
""")
    DeviceStatistics getStatistics(
            @Param("available") DeviceState available,
            @Param("assigned") DeviceState assigned,
            @Param("underRepair") DeviceState underRepair
    );

    interface DeviceStatistics {

        long getTotalDevices();

        long getAvailableDevices();

        long getAssignedDevices();

        long getUnderRepairDevices();
    }
}