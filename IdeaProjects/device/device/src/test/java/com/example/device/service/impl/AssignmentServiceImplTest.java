package com.example.device.service.impl;

import com.example.device.dto.request.DeviceAssignmentRequest;
import com.example.device.dto.request.ReturnDeviceRequest;
import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.enums.DeviceReturnCondition;
import com.example.device.enums.DeviceState;
import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import com.example.device.mapper.DeviceAssignmentMapper;
import com.example.device.model.Device;
import com.example.device.model.DeviceAssignment;
import com.example.device.model.User;
import com.example.device.repository.DeviceAssignmentRepository;
import com.example.device.repository.DeviceRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceImplTest {

    @Mock
    private DeviceAssignmentRepository deviceAssignmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceAssignmentMapper deviceAssignmentMapper;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void assignDevice_deviceNotAvailable_shouldThrowException() {
        UUID userId = UUID.randomUUID();
        UUID deviceId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .email("employee@gmail.com")
                .build();

        Device device = Device.builder()
                .id(deviceId)
                .state(DeviceState.ASSIGNED)
                .build();

        DeviceAssignmentRequest request = DeviceAssignmentRequest.builder()
                .userId(userId)
                .deviceId(deviceId)
                .expectedReturnAt(LocalDateTime.now().plusDays(3))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(deviceRepository.findById(deviceId)).thenReturn(Optional.of(device));
        when(deviceAssignmentRepository.existsByDeviceIdAndStatusIn(
                deviceId,
                List.of(DeviceAssignmentStatus.ACTIVE, DeviceAssignmentStatus.OVERDUE)
        )).thenReturn(false);

        AppException exception = assertThrows(
                AppException.class,
                () -> assignmentService.assignDevice(request)
        );

        assertSame(ErrorCode.DEVICE_NOT_AVAILABLE, exception.getErrorCode());
        verify(deviceAssignmentRepository, never()).save(any());
    }

    @Test
    void returnDevice_good_shouldSetDeviceAvailable() {
        UUID assignmentId = UUID.randomUUID();

        Device device = Device.builder()
                .id(UUID.randomUUID())
                .state(DeviceState.ASSIGNED)
                .build();

        DeviceAssignment assignment = DeviceAssignment.builder()
                .id(assignmentId)
                .device(device)
                .status(DeviceAssignmentStatus.ACTIVE)
                .build();

        ReturnDeviceRequest request = new ReturnDeviceRequest();
        request.setCondition(DeviceReturnCondition.GOOD);

        loginAs("it@gmail.com");
        mockUpdateReturnMapper();

        when(deviceAssignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(assignment));

        assignmentService.returnDevice(assignmentId, request);

        assertEquals(DeviceAssignmentStatus.RETURNED, assignment.getStatus());
        assertEquals(DeviceReturnCondition.GOOD, assignment.getReturnCondition());
        assertEquals(DeviceState.AVAILABLE, device.getState());
        assertEquals("it@gmail.com", device.getUpdatedBy());
        assertNotNull(assignment.getReturnedAt());

        verify(deviceAssignmentMapper).updateReturn(request, assignment);
    }

    @Test
    void returnDevice_damaged_shouldSetDeviceUnderRepair() {
        UUID assignmentId = UUID.randomUUID();

        Device device = Device.builder()
                .id(UUID.randomUUID())
                .state(DeviceState.ASSIGNED)
                .build();

        DeviceAssignment assignment = DeviceAssignment.builder()
                .id(assignmentId)
                .device(device)
                .status(DeviceAssignmentStatus.ACTIVE)
                .build();

        ReturnDeviceRequest request = new ReturnDeviceRequest();
        request.setCondition(DeviceReturnCondition.DAMAGED);
        request.setNote("Màn hình bị sọc");

        loginAs("it@gmail.com");
        mockUpdateReturnMapper();

        when(deviceAssignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(assignment));

        assignmentService.returnDevice(assignmentId, request);

        assertEquals(DeviceAssignmentStatus.RETURNED, assignment.getStatus());
        assertEquals(DeviceReturnCondition.DAMAGED, assignment.getReturnCondition());
        assertEquals("Màn hình bị sọc", assignment.getReturnNote());
        assertEquals(DeviceState.UNDER_REPAIR, device.getState());
        assertEquals("it@gmail.com", device.getUpdatedBy());

        verify(deviceAssignmentMapper).updateReturn(request, assignment);
    }

    @Test
    void returnDevice_damagedWithoutNote_shouldThrowException() {
        UUID assignmentId = UUID.randomUUID();

        DeviceAssignment assignment = DeviceAssignment.builder()
                .id(assignmentId)
                .device(Device.builder().state(DeviceState.ASSIGNED).build())
                .status(DeviceAssignmentStatus.ACTIVE)
                .build();

        ReturnDeviceRequest request = new ReturnDeviceRequest();
        request.setCondition(DeviceReturnCondition.DAMAGED);

        when(deviceAssignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(assignment));

        AppException exception = assertThrows(
                AppException.class,
                () -> assignmentService.returnDevice(assignmentId, request)
        );

        assertSame(ErrorCode.RETURN_NOTE_REQUIRED, exception.getErrorCode());
    }

    @Test
    void returnDevice_alreadyReturned_shouldThrowException() {
        UUID assignmentId = UUID.randomUUID();

        DeviceAssignment assignment = DeviceAssignment.builder()
                .id(assignmentId)
                .status(DeviceAssignmentStatus.RETURNED)
                .build();

        ReturnDeviceRequest request = new ReturnDeviceRequest();
        request.setCondition(DeviceReturnCondition.GOOD);

        when(deviceAssignmentRepository.findById(assignmentId))
                .thenReturn(Optional.of(assignment));

        AppException exception = assertThrows(
                AppException.class,
                () -> assignmentService.returnDevice(assignmentId, request)
        );

        assertSame(ErrorCode.DEVICE_ALREADY_RETURNED, exception.getErrorCode());
    }

    private void loginAs(String email) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void mockUpdateReturnMapper() {
        doAnswer(invocation -> {
            ReturnDeviceRequest request = invocation.getArgument(0);
            DeviceAssignment assignment = invocation.getArgument(1);

            assignment.setReturnCondition(request.getCondition());
            assignment.setReturnNote(request.getNote());

            return null;
        }).when(deviceAssignmentMapper)
                .updateReturn(any(ReturnDeviceRequest.class), any(DeviceAssignment.class));
    }
}