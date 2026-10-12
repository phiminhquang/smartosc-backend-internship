package com.example.device.service.impl;

import com.example.device.enums.EmailJobStatus;
import com.example.device.enums.EmailJobType;
import com.example.device.model.EmailJob;
import com.example.device.observability.RequestCorrelation;
import com.example.device.repository.EmailJobRepository;
import com.example.device.service.EmailJobEnqueueCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailJobEnqueueServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-10-11T03:00:00Z");

    @Mock
    private EmailJobRepository emailJobRepository;

    private EmailJobEnqueueServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EmailJobEnqueueServiceImpl(
                emailJobRepository,
                new ObjectMapper(),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void enqueueStoresSnapshotAndHashedIdempotencyKey() {
        when(emailJobRepository.save(any())).thenAnswer(invocation -> {
            EmailJob job = invocation.getArgument(0);
            job.setId(UUID.randomUUID());
            return job;
        });
        UUID assignmentId = UUID.randomUUID();
        LocalDateTime dueAt = LocalDateTime.of(2026, 10, 12, 9, 30);
        EmailJobEnqueueCommand command = new EmailJobEnqueueCommand(
                EmailJobType.ASSIGNMENT_REMINDER,
                "employee@device.test",
                "Reminder",
                "email/device-reminder",
                Map.of("deviceName", "Laptop"),
                "assignment-reminder:" + assignmentId + ":user-1",
                assignmentId,
                dueAt
        );

        try (RequestCorrelation.Scope ignored = RequestCorrelation.open("request-enqueue-123")) {
            assertTrue(service.enqueue(command));
        }

        ArgumentCaptor<EmailJob> captor = ArgumentCaptor.forClass(EmailJob.class);
        verify(emailJobRepository).save(captor.capture());
        EmailJob job = captor.getValue();
        assertEquals(EmailJobStatus.PENDING, job.getStatus());
        assertEquals("request-enqueue-123", job.getRequestId());
        assertEquals(5, job.getMaxAttempts());
        assertEquals(NOW, job.getAvailableAt());
        assertEquals(assignmentId, job.getAssignmentId());
        assertEquals(dueAt, job.getAssignmentExpectedReturnAt());
        assertTrue(job.getPayload().contains("Laptop"));
        assertFalse(job.getIdempotencyKey().contains(assignmentId.toString()));
        assertEquals(64, job.getIdempotencyKey().length());
    }

    @Test
    void duplicateLogicalJobIsNotSavedAgain() {
        when(emailJobRepository.existsByIdempotencyKey(any())).thenReturn(true);
        EmailJobEnqueueCommand command = new EmailJobEnqueueCommand(
                EmailJobType.DAILY_OVERDUE_REPORT,
                "admin@device.test",
                "Report",
                "email/overdue-report",
                Map.of("total", 1),
                "daily-overdue-report:2026-10-11:admin-1",
                null,
                null
        );

        assertFalse(service.enqueue(command));
        verify(emailJobRepository, never()).save(any());
    }

    @Test
    void assignmentTypeRequiresAssignmentSnapshotMetadata() {
        EmailJobEnqueueCommand command = new EmailJobEnqueueCommand(
                EmailJobType.ASSIGNMENT_OVERDUE,
                "employee@device.test",
                "Overdue",
                "email/device-overdue",
                Map.of(),
                "assignment-overdue:missing",
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, () -> service.enqueue(command));
        verify(emailJobRepository, never()).save(any());
    }
}
