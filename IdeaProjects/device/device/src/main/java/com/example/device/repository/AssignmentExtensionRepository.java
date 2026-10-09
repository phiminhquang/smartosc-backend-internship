package com.example.device.repository;

import com.example.device.enums.ExtensionRequestStatus;
import com.example.device.model.AssignmentExtension;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface AssignmentExtensionRepository extends JpaRepository<AssignmentExtension, UUID>, JpaSpecificationExecutor<AssignmentExtension> {

    @Override
    @EntityGraph(attributePaths = {"assignment", "assignment.device"})
    Page<AssignmentExtension> findAll(Specification<AssignmentExtension> specification, Pageable pageable);

    boolean existsByAssignmentIdAndStatus(UUID assignmentId, ExtensionRequestStatus status);

    @Query("select e.assignment.id from AssignmentExtension e where e.id = :requestId")
    Optional<UUID> findAssignmentIdByRequestId(@Param("requestId") UUID requestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from AssignmentExtension e where e.id = :requestId")
    Optional<AssignmentExtension> findByIdForUpdate(@Param("requestId") UUID requestId);

    @Query("""
    select e from AssignmentExtension e
    join fetch e.assignment a
    join fetch a.device
    where e.id = :requestId
""")
    Optional<AssignmentExtension> findByIdWithDetails(@Param("requestId") UUID requestId);

}
