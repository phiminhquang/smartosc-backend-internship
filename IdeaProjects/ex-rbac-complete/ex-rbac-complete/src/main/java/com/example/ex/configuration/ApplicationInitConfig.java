package com.example.ex.configuration;

import com.example.ex.enums.RoleName;
import com.example.ex.model.Roles;
import com.example.ex.model.User;
import com.example.ex.repository.RoleRepository;
import com.example.ex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class  ApplicationInitConfig implements ApplicationRunner {

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
        seedRole(RoleName.ADMIN, "Full system access");
        seedRole(RoleName.EDITOR, "Can view and edit allowed resources");
        seedRole(RoleName.VIEWER, "Basic user role");

        if (!userRepository.existsByEmail(adminEmail)) {
            Roles adminRole = roleRepository.findByName(RoleName.ADMIN.name())
                    .orElseThrow();

            User admin = User.builder()
                    .email(adminEmail)
                    .name(adminName)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .roles(new HashSet<>())
                    .build();

            admin.getRoles().add(adminRole);
            userRepository.save(admin);
        }
    }

    private void seedRole(RoleName roleName, String description) {
        if (!roleRepository.existsByName(roleName.name())) {
            roleRepository.save(Roles.builder()
                    .name(roleName.name())
                    .description(description)
                    .build());
        }
    }
}
