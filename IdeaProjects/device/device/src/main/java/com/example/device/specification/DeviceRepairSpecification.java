package com.example.device.specification;

import com.example.device.enums.RepairStatus;
import com.example.device.model.DeviceRepair;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class DeviceRepairSpecification {

    private DeviceRepairSpecification() {
    }

    public static Specification<DeviceRepair> hasStatus(RepairStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<DeviceRepair> hasDeviceId(UUID deviceId) {
        return (root, query, criteriaBuilder) ->
                deviceId == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("device").get("id"), deviceId);
    }
}
