package com.example.device.mapper;

import com.example.device.dto.request.ExtensionRequestCreationRequest;
import com.example.device.dto.request.ExtensionReviewRequest;
import com.example.device.dto.response.ExtensionResponse;
import com.example.device.model.AssignmentExtension;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AssignmentExtensionMapper {

    @Mapping(source = "assignment.id", target = "assignmentId")
    @Mapping(source = "assignment.device.id", target = "deviceId")
    @Mapping(source = "assignment.device.name", target = "deviceName")
    @Mapping(source = "assignment.device.serialNumber", target = "deviceSerialNumber")
    ExtensionResponse toResponse(AssignmentExtension extension);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "requestedReturnAt", target = "requestedReturnAt")
    @Mapping(source = "reason", target = "reason")
    AssignmentExtension toEntity(ExtensionRequestCreationRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(source = "note", target = "reviewNote")
    void updateReview(ExtensionReviewRequest request, @MappingTarget AssignmentExtension extension);
}