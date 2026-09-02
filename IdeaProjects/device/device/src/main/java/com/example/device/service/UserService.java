package com.example.device.service;

import com.example.device.dto.request.UserCreationRequest;
import com.example.device.dto.request.UserUpdateRequest;
import com.example.device.dto.response.UserCreationResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserCreationResponse createUser(UserCreationRequest request);

    List<UserCreationResponse> getUsers();

    UserCreationResponse getUser(UUID userId);

    UserCreationResponse updateUser(UUID userId, UserUpdateRequest request);

    void deleteUser(UUID userId);

    UserCreationResponse assignRole(UUID userId, UUID roleId);

    UserCreationResponse removeRole(UUID userId, UUID roleId);

    UserCreationResponse getCurrentUser();
}