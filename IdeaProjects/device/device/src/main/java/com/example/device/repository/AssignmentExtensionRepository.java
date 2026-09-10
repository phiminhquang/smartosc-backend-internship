package com.example.device.repository;

import com.example.device.enums.ExtensionRequestStatus;
import com.example.device.model.AssignmentExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface AssignmentExtensionRepository extends JpaRepository<AssignmentExtension, UUID> {

    boolean existsByAssignmentIdAndStatus(UUID assignmentId, ExtensionRequestStatus status);

    @Query("""
    select e from AssignmentExtension e
    join fetch e.assignment a
    join fetch a.device
    where e.status = :status
    order by e.requestedAt asc
""")
    List<AssignmentExtension> findByStatusOrderByRequestedAtAsc(@Param("status") ExtensionRequestStatus status);

    @Query("""
    select e from AssignmentExtension e
    join fetch e.assignment a
    join fetch a.device
    where e.requestedBy = :requestedBy
    order by e.requestedAt desc
""")
    List<AssignmentExtension> findByRequestedByOrderByRequestedAtDesc(@Param("requestedBy") String requestedBy);

    @Query("""
    select e from AssignmentExtension e
    join fetch e.assignment a
    join fetch a.device
    where e.id = :requestId
""")
    Optional<AssignmentExtension> findByIdWithDetails(@Param("requestId") UUID requestId);

}