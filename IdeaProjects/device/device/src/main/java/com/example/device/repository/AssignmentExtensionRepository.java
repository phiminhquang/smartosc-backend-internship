package com.example.device.repository;

import com.example.device.enums.ExtensionRequestStatus;
import com.example.device.model.AssignmentExtension;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssignmentExtensionRepository extends JpaRepository<AssignmentExtension, UUID> {

    boolean existsByAssignmentIdAndStatus(UUID assignmentId, ExtensionRequestStatus status);

    List<AssignmentExtension> findByStatusOrderByRequestedAtAsc(ExtensionRequestStatus status);

    List<AssignmentExtension> findByRequestedByOrderByRequestedAtDesc(String requestedBy);
}