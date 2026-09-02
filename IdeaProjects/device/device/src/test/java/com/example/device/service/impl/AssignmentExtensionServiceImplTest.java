package com.example.device.service.impl;

import com.example.device.dto.request.ExtensionRequestCreationRequest;
import com.example.device.dto.request.ExtensionReviewRequest;
import com.example.device.dto.response.ExtensionResponse;
import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.enums.ExtensionRequestStatus;
import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import com.example.device.mapper.AssignmentExtensionMapper;
import com.example.device.model.AssignmentExtension;
import com.example.device.model.Device;
import com.example.device.model.DeviceAssignment;
import com.example.device.model.User;
import com.example.device.repository.AssignmentExtensionRepository;
import com.example.device.repository.DeviceAssignmentRepository;
import com.example.device.repository.UserRepository;
import com.example.device.service.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentExtensionServiceImplTest {

    @Mock
    private AssignmentExtensionRepository extensionRepository;

    @Mock
    private DeviceAssignmentRepository assignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private AssignmentExtensionMapper extensionMapper;

    @InjectMocks
    private AssignmentExtensionServiceImpl extensionService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createRequest_otherUserAssignment_shouldThrowUnauthorized() {
        UUID assignmentId = UUID.randomUUID();

        User owner = User.builder()
                .id(UUID.randomUUID())
                .email("owner@gmail.com")
                .build();

        User currentUser = User.builder()
                .id(UUID.randomUUID())
                .email("employee@gmail.com")
                .build();

        DeviceAssignment assignment = DeviceAssignment.builder()
                .id(assignmentId)
                .user(owner)
                .status(DeviceAssignmentStatus.ACTIVE)
                .expectedReturnAt(LocalDateTime.now().plusDays(2))
                .build();

        ExtensionRequestCreationRequest request =
                new ExtensionRequestCreationRequest();

        request.setRequestedReturnAt(LocalDateTime.now().plusDays(5));
        request.setReason("Cần dùng thêm");

        loginAs("employee@gmail.com");

        when(assignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(assignment));

        when(userRepository.findByEmail("employee@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        AppException exception = assertThrows(
                AppException.class,
                () -> extensionService.createRequest(assignmentId, request)
        );

        assertSame(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        verify(extensionRepository, never()).save(any());
    }

    @Test
    void createRequest_invalidDate_shouldThrowException() {
        UUID userId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();

        User employee = User.builder()
                .id(userId)
                .email("employee@gmail.com")
                .build();

        LocalDateTime currentDeadline = LocalDateTime.now().plusDays(5);

        DeviceAssignment assignment = DeviceAssignment.builder()
                .id(assignmentId)
                .user(employee)
                .status(DeviceAssignmentStatus.ACTIVE)
                .expectedReturnAt(currentDeadline)
                .build();

        ExtensionRequestCreationRequest request =
                new ExtensionRequestCreationRequest();

        request.setRequestedReturnAt(currentDeadline.minusDays(1));
        request.setReason("Xin gia hạn");

        loginAs("employee@gmail.com");

        when(assignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(assignment));

        when(userRepository.findByEmail("employee@gmail.com"))
                .thenReturn(Optional.of(employee));

        AppException exception = assertThrows(
                AppException.class,
                () -> extensionService.createRequest(assignmentId, request)
        );

        assertSame(ErrorCode.INVALID_EXTENSION_DATE, exception.getErrorCode());
    }

    @Test
    void approveRequest_shouldUpdateDeadline() {
        UUID requestId = UUID.randomUUID();

        LocalDateTime oldDeadline = LocalDateTime.now().plusDays(2);
        LocalDateTime newDeadline = LocalDateTime.now().plusDays(7);

        User employee = User.builder()
                .id(UUID.randomUUID())
                .email("employee@gmail.com")
                .name("Employee")
                .build();

        User admin = User.builder()
                .id(UUID.randomUUID())
                .email("admin@gmail.com")
                .name("Admin")
                .build();

        Device device = Device.builder()
                .name("Dell Latitude")
                .build();

        DeviceAssignment assignment = DeviceAssignment.builder()
                .id(UUID.randomUUID())
                .user(employee)
                .device(device)
                .status(DeviceAssignmentStatus.ACTIVE)
                .expectedReturnAt(oldDeadline)
                .reminderNotifiedAt(LocalDateTime.now())
                .overdueNotifiedAt(LocalDateTime.now())
                .build();

        AssignmentExtension extension = AssignmentExtension.builder()
                .id(requestId)
                .assignment(assignment)
                .previousReturnAt(oldDeadline)
                .requestedReturnAt(newDeadline)
                .status(ExtensionRequestStatus.PENDING)
                .requestedBy("employee@gmail.com")
                .build();

        ExtensionReviewRequest reviewRequest = new ExtensionReviewRequest();
        reviewRequest.setNote("Đồng ý");

        loginAs("admin@gmail.com");

        when(extensionRepository.findById(requestId))
                .thenReturn(Optional.of(extension));

        when(userRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.of(admin));

        when(extensionMapper.toResponse(any()))
                .thenReturn(new ExtensionResponse());

        extensionService.approveRequest(requestId, reviewRequest);

        assertEquals(newDeadline, assignment.getExpectedReturnAt());
        assertEquals(DeviceAssignmentStatus.ACTIVE, assignment.getStatus());
        assertNull(assignment.getReminderNotifiedAt());
        assertNull(assignment.getOverdueNotifiedAt());

        assertEquals(ExtensionRequestStatus.APPROVED, extension.getStatus());
        assertEquals("admin@gmail.com", extension.getReviewedBy());
        assertNotNull(extension.getReviewedAt());

        verify(emailService).sendEmail(
                eq("employee@gmail.com"),
                anyString(),
                anyString()
        );
    }

    private void loginAs(String email) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}