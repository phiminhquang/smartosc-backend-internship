package com.example.device.service.impl;

import com.example.device.dto.request.RepairCompleteRequest;
import com.example.device.dto.request.RepairUnrepairableRequest;
import com.example.device.enums.DeviceState;
import com.example.device.enums.RepairStatus;
import com.example.device.mapper.DeviceRepairMapper;
import com.example.device.model.Device;
import com.example.device.model.DeviceRepair;
import com.example.device.repository.DeviceRepairRepository;
import com.example.device.repository.DeviceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceRepairServiceImplTest {

    @Mock
    private DeviceRepairRepository repairRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceRepairMapper repairMapper;

    @InjectMocks
    private DeviceRepairServiceImpl repairService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void startRepair_pending_shouldBecomeInProgress() {
        UUID repairId = UUID.randomUUID();

        DeviceRepair repair = DeviceRepair.builder()
                .id(repairId)
                .status(RepairStatus.PENDING)
                .build();

        loginAs("it@gmail.com");

        when(repairRepository.findById(repairId))
                .thenReturn(Optional.of(repair));

        repairService.startRepair(repairId);

        assertEquals(RepairStatus.IN_PROGRESS, repair.getStatus());
        assertEquals("it@gmail.com", repair.getStartedBy());
        assertNotNull(repair.getStartedAt());
    }

    @Test
    void completeRepair_shouldMakeDeviceAvailable() {
        UUID repairId = UUID.randomUUID();

        Device device = Device.builder()
                .id(UUID.randomUUID())
                .state(DeviceState.UNDER_REPAIR)
                .build();

        DeviceRepair repair = DeviceRepair.builder()
                .id(repairId)
                .device(device)
                .status(RepairStatus.IN_PROGRESS)
                .build();

        RepairCompleteRequest request = new RepairCompleteRequest();
        request.setRepairNote("Đã sửa xong");
        request.setCost(BigDecimal.valueOf(500000));

        loginAs("it@gmail.com");

        when(repairRepository.findById(repairId))
                .thenReturn(Optional.of(repair));

        repairService.completeRepair(repairId, request);

        assertEquals(RepairStatus.COMPLETED, repair.getStatus());
        assertEquals(DeviceState.AVAILABLE, device.getState());
        assertEquals("it@gmail.com", repair.getFinishedBy());
        assertNotNull(repair.getFinishedAt());

        verify(repairMapper).updateComplete(request, repair);
    }

    @Test
    void markUnrepairable_shouldKeepDeviceUnderRepair() {
        UUID repairId = UUID.randomUUID();

        Device device = Device.builder()
                .id(UUID.randomUUID())
                .state(DeviceState.UNDER_REPAIR)
                .build();

        DeviceRepair repair = DeviceRepair.builder()
                .id(repairId)
                .device(device)
                .status(RepairStatus.IN_PROGRESS)
                .build();

        RepairUnrepairableRequest request = new RepairUnrepairableRequest();
        request.setRepairNote("Mainboard hỏng không thể sửa");

        loginAs("it@gmail.com");

        when(repairRepository.findById(repairId))
                .thenReturn(Optional.of(repair));

        repairService.markUnrepairable(repairId, request);

        assertEquals(RepairStatus.UNREPAIRABLE, repair.getStatus());
        assertEquals(DeviceState.UNDER_REPAIR, device.getState());
        assertEquals("it@gmail.com", repair.getFinishedBy());

        verify(repairMapper).updateUnrepairable(request, repair);
    }

    private void loginAs(String email) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}