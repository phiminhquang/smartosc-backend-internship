package com.example.device.service;

import com.example.device.dto.request.DeviceCreationRequest;
import com.example.device.dto.request.DeviceStatusUpdateRequest;
import com.example.device.dto.request.DeviceUpdateRequest;
import com.example.device.dto.response.DeviceResponse;
import com.example.device.enums.DeviceCategory;
import com.example.device.enums.DeviceState;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface DeviceService {

    DeviceResponse createDevice(DeviceCreationRequest request);

    List<DeviceResponse> getDevices();

    DeviceResponse getDeviceById(UUID id);

    DeviceResponse updateDevice(UUID id, DeviceUpdateRequest request);

    void deleteDevice(UUID id);

    DeviceResponse updateDeviceState(
            UUID id,
            DeviceStatusUpdateRequest request
    );

    Page<DeviceResponse> searchDevices(
            String keyword,
            DeviceState state,
            DeviceCategory category,
            int page,
            int size
    );

}