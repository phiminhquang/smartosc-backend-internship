package com.example.device.specification;

import com.example.device.enums.DeviceCategory;
import com.example.device.enums.DeviceState;
import com.example.device.model.Device;
import org.springframework.data.jpa.domain.Specification;

public class DeviceSpecification {

    private DeviceSpecification() {
    }

    public static Specification<Device> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return cb.conjunction();

            String value = "%" + keyword.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), value),
                    cb.like(cb.lower(root.get("model")), value),
                    cb.like(cb.lower(root.get("serialNumber")), value)
            );
        };
    }

    public static Specification<Device> hasState(DeviceState state) {
        return (root, query, cb) ->
                state == null ? cb.conjunction() : cb.equal(root.get("state"), state);
    }

    public static Specification<Device> hasCategory(DeviceCategory category) {
        return (root, query, cb) ->
                category == null ? cb.conjunction() : cb.equal(root.get("category"), category);
    }
}