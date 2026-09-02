package com.example.device.service;

import com.example.device.dto.request.RoleCreationRequest;
import com.example.device.dto.request.RoleUpdateRequest;
import com.example.device.dto.response.RoleResponse;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    List<RoleResponse> getRoles();

    RoleResponse getRoleById(UUID roleId);

    RoleResponse createRole(RoleCreationRequest request);

    RoleResponse updateRole(UUID roleId, RoleUpdateRequest request);

    void deleteRole(UUID roleId);
}