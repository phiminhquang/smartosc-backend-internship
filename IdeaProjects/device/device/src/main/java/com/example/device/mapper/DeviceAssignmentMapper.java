package com.example.device.mapper;

import com.example.device.dto.request.ReturnDeviceRequest;
import com.example.device.dto.response.DeviceAssignmentResponse;
import com.example.device.model.DeviceAssignment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DeviceAssignmentMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "device.id", target = "deviceId")
    @Mapping(source = "device.name", target = "deviceName")
    @Mapping(source = "device.serialNumber", target = "deviceSerialNumber")
    DeviceAssignmentResponse toResponse(DeviceAssignment assignment);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "condition", target = "returnCondition")
    @Mapping(source = "note", target = "returnNote")
    void updateReturn(ReturnDeviceRequest request, @MappingTarget DeviceAssignment assignment);
}