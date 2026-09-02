package com.example.device.configuration;

import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import com.example.device.model.Role;
import com.example.device.model.User;
import com.example.device.repository.RoleRepository;
import com.example.device.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ApplicationInitConfig implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name:Administrator}")
    private String adminName;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRole("ADMIN", "Full system access");
        seedRole("IT_STAFF", "Can view and edit allowed resources");
        seedRole("EMPLOYEE", "Use assigned devices and request extensions");

        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

            User admin = User.builder()
                    .email(adminEmail)
                    .name(adminName)
                    .password(passwordEncoder.encode(adminPassword))
                    .systemOwner(true)
                    .build();

            admin.getRoles().add(adminRole);
            userRepository.save(admin);
        }
    }

    private void seedRole(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            roleRepository.save(Role.builder()
                    .name(name)
                    .description(description)
                    .build());
        }
    }
}