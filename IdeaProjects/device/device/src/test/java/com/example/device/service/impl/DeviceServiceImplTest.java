package com.example.device.service.impl;

import com.example.device.dto.response.DeviceResponse;
import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import com.example.device.mapper.DeviceMapper;
import com.example.device.model.Device;
import com.example.device.repository.DeviceAssignmentRepository;
import com.example.device.repository.DeviceRepository;
import com.example.device.service.DeviceSerialNumberGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceAssignmentRepository deviceAssignmentRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @Mock
    private DeviceSerialNumberGenerator serialNumberGenerator;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    @Test
    void getDeviceById_success() {
        UUID id = UUID.randomUUID();

        Device device = Device.builder()
                .id(id)
                .name("Dell Latitude")
                .build();

        DeviceResponse response = DeviceResponse.builder()
                .id(id.toString())
                .name("Dell Latitude")
                .build();

        when(deviceRepository.findById(id)).thenReturn(Optional.of(device));
        when(deviceMapper.toDeviceResponse(device)).thenReturn(response);

        DeviceResponse result = deviceService.getDeviceById(id);

        assertEquals(id.toString(), result.getId());
        assertEquals("Dell Latitude", result.getName());

        verify(deviceRepository).findById(id);
        verify(deviceMapper).toDeviceResponse(device);
    }

    @Test
    void getDeviceById_notFound_shouldThrowException() {
        UUID id = UUID.randomUUID();

        when(deviceRepository.findById(id)).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> deviceService.getDeviceById(id)
        );

        assertSame(ErrorCode.DEVICE_NOT_FOUND, exception.getErrorCode());
        verify(deviceMapper, never()).toDeviceResponse(any());
    }
}