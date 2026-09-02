package com.example.device.mapper;

import com.example.device.dto.request.RepairCompleteRequest;
import com.example.device.dto.request.RepairCreationRequest;
import com.example.device.dto.request.RepairUnrepairableRequest;
import com.example.device.dto.response.RepairResponse;
import com.example.device.model.DeviceRepair;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DeviceRepairMapper {

    @Mapping(source = "device.id", target = "deviceId")
    @Mapping(source = "device.name", target = "deviceName")
    @Mapping(source = "device.serialNumber", target = "deviceSerialNumber")
    RepairResponse toResponse(DeviceRepair repair);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "issueDescription", target = "issueDescription")
    DeviceRepair toEntity(RepairCreationRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "repairNote", target = "repairNote")
    @Mapping(source = "cost", target = "cost")
    void updateComplete(RepairCompleteRequest request, @MappingTarget DeviceRepair repair);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "repairNote", target = "repairNote")
    void updateUnrepairable(RepairUnrepairableRequest request, @MappingTarget DeviceRepair repair);
}