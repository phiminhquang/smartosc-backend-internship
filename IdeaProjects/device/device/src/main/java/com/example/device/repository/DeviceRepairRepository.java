package com.example.device.repository;

import com.example.device.enums.RepairStatus;
import com.example.device.model.DeviceRepair;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepairRepository extends JpaRepository<DeviceRepair, UUID> {

    boolean existsByDeviceId(UUID deviceId);
    boolean existsByDeviceIdAndStatusIn(UUID deviceId, List<RepairStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from DeviceRepair r where r.id = :repairId")
    Optional<DeviceRepair> findByIdForUpdate(@Param("repairId") UUID repairId);

    @Query("""
    select r from DeviceRepair r
    join fetch r.device
    order by r.createdAt desc
""")
    List<DeviceRepair> findAllByOrderByCreatedAtDesc();

    @Query("""
    select r from DeviceRepair r
    join fetch r.device
    where r.device.id = :deviceId
    order by r.createdAt desc
""")
    List<DeviceRepair> findByDeviceIdOrderByCreatedAtDesc(@Param("deviceId") UUID deviceId);

    @Query("""
    select
        count(r) as totalRepairs,
        coalesce(sum(case when r.status = :pending then 1 else 0 end), 0) as pendingRepairs,
        coalesce(sum(case when r.status = :inProgress then 1 else 0 end), 0) as inProgressRepairs,
        coalesce(sum(case when r.status = :completed then 1 else 0 end), 0) as completedRepairs,
        coalesce(sum(case when r.status = :unrepairable then 1 else 0 end), 0) as unrepairableRepairs
    from DeviceRepair r
""")
    RepairStatistics getStatistics(
            @Param("pending") RepairStatus pending,
            @Param("inProgress") RepairStatus inProgress,
            @Param("completed") RepairStatus completed,
            @Param("unrepairable") RepairStatus unrepairable
    );

    @Query("""
    select r from DeviceRepair r
    join fetch r.device
    where r.id = :repairId
""")
    Optional<DeviceRepair> findByIdWithDevice(@Param("repairId") UUID repairId);

    interface RepairStatistics {
        long getTotalRepairs();
        long getPendingRepairs();
        long getInProgressRepairs();
        long getCompletedRepairs();
        long getUnrepairableRepairs();
    }
}
