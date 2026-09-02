package com.example.device.mapper;

import com.example.device.dto.request.UserCreationRequest;
import com.example.device.dto.request.UserUpdateRequest;
import com.example.device.dto.response.UserCreationResponse;
import com.example.device.model.Role;
import com.example.device.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toUser(UserCreationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "systemOwner", ignore = true)
    void updateUser(UserUpdateRequest request, @MappingTarget User user);

    UserCreationResponse toUserResponse(User user);

    default String mapRoleToString(Role role) {
        return role.getName();
    }
}