package com.example.device.specification;

import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.model.DeviceAssignment;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class DeviceAssignmentSpecification {

    private DeviceAssignmentSpecification() {
    }

    public static Specification<DeviceAssignment> hasStatus(DeviceAssignmentStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<DeviceAssignment> hasUserId(UUID userId) {
        return (root, query, criteriaBuilder) ->
                userId == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<DeviceAssignment> hasDeviceId(UUID deviceId) {
        return (root, query, criteriaBuilder) ->
                deviceId == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("device").get("id"), deviceId);
    }

    public static Specification<DeviceAssignment> hasUserEmail(String email) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("email"), email);
    }
}
