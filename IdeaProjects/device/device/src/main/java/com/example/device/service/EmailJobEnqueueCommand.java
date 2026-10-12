package com.example.device.service;

import com.example.device.enums.EmailJobType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record EmailJobEnqueueCommand(
        EmailJobType jobType,
        String recipient,
        String subject,
        String templateKey,
        Map<String, Object> variables,
        String logicalKey,
        UUID assignmentId,
        LocalDateTime assignmentExpectedReturnAt
) {
}
