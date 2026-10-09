package com.example.device.specification;

import com.example.device.enums.ExtensionRequestStatus;
import com.example.device.model.AssignmentExtension;
import org.springframework.data.jpa.domain.Specification;

public final class AssignmentExtensionSpecification {

    private AssignmentExtensionSpecification() {
    }

    public static Specification<AssignmentExtension> hasStatus(ExtensionRequestStatus status) {
        return (root, query, criteriaBuilder) ->
                status == null
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<AssignmentExtension> wasRequestedBy(String email) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("requestedBy"), email);
    }
}
