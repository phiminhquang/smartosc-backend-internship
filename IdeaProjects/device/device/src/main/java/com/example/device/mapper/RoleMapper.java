package com.example.device.mapper;

import com.example.device.dto.request.RoleCreationRequest;
import com.example.device.dto.request.RoleUpdateRequest;
import com.example.device.dto.response.RoleResponse;
import com.example.device.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toResponse(Role role);

    Role toRole(RoleCreationRequest request);

    @Mapping(target = "id", ignore = true)
    void updateRole(RoleUpdateRequest request, @MappingTarget Role role);
}