package com.example.device.repository;

import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.model.DeviceAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceAssignmentRepository extends JpaRepository<DeviceAssignment, UUID> {

    boolean existsByDeviceId(UUID deviceId);

    boolean existsByUserId(UUID userId);

    boolean existsByDeviceIdAndStatusIn(UUID deviceId, List<DeviceAssignmentStatus> statuses);


    @Query("""
    select a
    from DeviceAssignment a
    join fetch a.user
    join fetch a.device
    where a.id = :assignmentId
""")
    Optional<DeviceAssignment> findByIdWithDetails(
            @Param("assignmentId") UUID assignmentId
    );

    @Query("""
    select a
    from DeviceAssignment a
    join fetch a.user
    join fetch a.device
    where a.status = :status
      and a.reminderNotifiedAt is null
      and a.expectedReturnAt > :start
      and a.expectedReturnAt < :end
""")
    List<DeviceAssignment>
    findByStatusAndReminderNotifiedAtIsNullAndExpectedReturnAtAfterAndExpectedReturnAtBefore(
            @Param("status") DeviceAssignmentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
    select a
    from DeviceAssignment a
    join fetch a.user
    join fetch a.device
    where a.status = :status
    order by a.expectedReturnAt asc
""")
    List<DeviceAssignment> findByStatusOrderByExpectedReturnAtAsc(
            @Param("status") DeviceAssignmentStatus status
    );

    @Query("""
    select a
    from DeviceAssignment a
    join fetch a.user
    join fetch a.device
    where a.user.email = :email
    order by a.assignedAt desc
""")
    List<DeviceAssignment> findByUser_EmailOrderByAssignedAtDesc(
            @Param("email") String email
    );

    @Query("""
    select
        count(a) as totalAssignments,
        coalesce(sum(case when a.status = :active then 1 else 0 end), 0) as activeAssignments,
        coalesce(sum(case when a.status = :overdue then 1 else 0 end), 0) as overdueAssignments,
        coalesce(sum(case when a.status = :returned then 1 else 0 end), 0) as returnedAssignments
    from DeviceAssignment a
""")
    AssignmentStatistics getStatistics(
            @Param("active") DeviceAssignmentStatus active,
            @Param("overdue") DeviceAssignmentStatus overdue,
            @Param("returned") DeviceAssignmentStatus returned
    );

    interface AssignmentStatistics {
        long getTotalAssignments();
        long getActiveAssignments();
        long getOverdueAssignments();
        long getReturnedAssignments();
    }

    @Query("""
    select a
    from DeviceAssignment a
    join fetch a.user
    join fetch a.device
    order by a.assignedAt desc
""")
    List<DeviceAssignment> findAllWithUserAndDevice();

    @Query("""
    select a
    from DeviceAssignment a
    join fetch a.user
    join fetch a.device
    where a.status = :status
    order by a.assignedAt desc
""")
    List<DeviceAssignment> findByStatusWithDetails(
            @Param("status") DeviceAssignmentStatus status
    );

    @Query("""
    select a
    from DeviceAssignment a
    join fetch a.user
    join fetch a.device
    where a.status = :status
      and a.overdueNotifiedAt is null
""")
    List<DeviceAssignment> findOverdueForNotification(
            @Param("status") DeviceAssignmentStatus status
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
    update DeviceAssignment a
    set a.status = :newStatus
    where a.status = :currentStatus
      and a.expectedReturnAt < :now
""")
    int updateOverdueAssignments(
            @Param("currentStatus")
            DeviceAssignmentStatus currentStatus,

            @Param("newStatus")
            DeviceAssignmentStatus newStatus,

            @Param("now")
            LocalDateTime now
    );

    @Query("""
    select a
    from DeviceAssignment a
    join fetch a.user
    join fetch a.device
    where a.user.id = :userId
    order by a.assignedAt desc
""")
    List<DeviceAssignment> findByUserIdOrderByAssignedAtDesc(
            @Param("userId") UUID userId
    );

}