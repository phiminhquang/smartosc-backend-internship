package com.example.device.mapper;

import com.example.device.dto.request.DeviceCreationRequest;
import com.example.device.dto.request.DeviceUpdateRequest;
import com.example.device.dto.response.DeviceResponse;
import com.example.device.model.Device;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "serialNumber", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    Device toDevice(DeviceCreationRequest request);

    DeviceResponse toDeviceResponse(Device device);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "serialNumber", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    void updateDevice(
            DeviceUpdateRequest request,
            @MappingTarget Device device
    );
}