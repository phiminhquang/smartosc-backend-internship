package com.example.device.service.impl;

import com.example.device.dto.request.UserCreationRequest;
import com.example.device.dto.request.UserUpdateRequest;
import com.example.device.dto.response.UserCreationResponse;
import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import com.example.device.mapper.UserMapper;
import com.example.device.model.Role;
import com.example.device.model.User;
import com.example.device.repository.DeviceAssignmentRepository;
import com.example.device.repository.RoleRepository;
import com.example.device.repository.UserRepository;
import com.example.device.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceimpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final DeviceAssignmentRepository deviceAssignmentRepository;

    @Override
    @Transactional
    public UserCreationResponse createUser(UserCreationRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setSystemOwner(false);

        Role defaultRole = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        user.getRoles().add(defaultRole);

        User savedUser = userRepository.save(user);

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public List<UserCreationResponse> getUsers() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @Override
    public UserCreationResponse getUser(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserCreationResponse updateUser(UUID userId, UserUpdateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.isSystemOwner()) {
            requireSystemOwner();
        }

        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {

            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        userMapper.updateUser(request, user);

        User updatedUser = userRepository.save(user);

        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (targetUser.isSystemOwner()) {
            throw new AppException(ErrorCode.CANNOT_DELETE_SYSTEM_OWNER);
        }

        if (deviceAssignmentRepository.existsByUserId(userId)) {
            throw new AppException(ErrorCode.USER_HAS_ASSIGNMENT_HISTORY);
        }

        userRepository.delete(targetUser);
    }

    @Override
    @Transactional
    public UserCreationResponse assignRole(UUID userId, UUID roleId) {

        User currentUser = findCurrentUser();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        // Chỉ System Owner mới được cấp ADMIN
        if ("ADMIN".equals(role.getName()) && !currentUser.isSystemOwner()) {
            throw new AppException(ErrorCode.CANNOT_CREATE_ADMIN);
        }

        // Không cho gán trùng role
        if (user.getRoles().contains(role)) {
            throw new AppException(ErrorCode.ROLE_ALREADY_ASSIGNED);
        }

        user.getRoles().add(role);

        User updatedUser = userRepository.save(user);

        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserCreationResponse removeRole(UUID userId, UUID roleId) {

        User currentUser = findCurrentUser();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        // Không cho Admin thường sửa role của System Owner
        if (user.isSystemOwner() && !currentUser.isSystemOwner()) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_SYSTEM_OWNER);
        }

        // User phải thực sự có role này
        if (!user.getRoles().contains(role)) {
            throw new AppException(ErrorCode.ROLE_NOT_ASSIGNED);
        }

        // Không cho user mất toàn bộ role
        if (user.getRoles().size() == 1) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_SYSTEM_OWNER);
        }

        user.getRoles().remove(role);

        User updatedUser = userRepository.save(user);

        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public UserCreationResponse getCurrentUser() {

        User user = findCurrentUser();

        return userMapper.toUserResponse(user);
    }

    private User findCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private void requireSystemOwner() {

        User currentUser = findCurrentUser();

        if (!currentUser.isSystemOwner()) {
            throw new AppException(ErrorCode.SYSTEM_OWNER_REQUIRED);
        }
    }
}