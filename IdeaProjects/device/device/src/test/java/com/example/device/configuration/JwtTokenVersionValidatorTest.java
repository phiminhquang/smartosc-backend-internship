package com.example.device.configuration;

import com.example.device.model.User;
import com.example.device.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenVersionValidatorTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void validate_matchingVersion_shouldSucceed() {
        User user = User.builder().email("user@example.com").tokenVersion(2).build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        JwtTokenVersionValidator validator = new JwtTokenVersionValidator(userRepository);

        OAuth2TokenValidatorResult result = validator.validate(jwt(user.getEmail(), 2));

        assertFalse(result.hasErrors());
    }

    @Test
    void validate_oldVersion_shouldFail() {
        User user = User.builder().email("user@example.com").tokenVersion(3).build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        JwtTokenVersionValidator validator = new JwtTokenVersionValidator(userRepository);

        OAuth2TokenValidatorResult result = validator.validate(jwt(user.getEmail(), 2));

        assertTrue(result.hasErrors());
    }

    @Test
    void validate_missingVersion_shouldFail() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS512")
                .subject("user@example.com")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
        JwtTokenVersionValidator validator = new JwtTokenVersionValidator(userRepository);

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertTrue(result.hasErrors());
    }

    private Jwt jwt(String subject, long version) {
        Instant now = Instant.now();
        return Jwt.withTokenValue("token")
                .header("alg", "HS512")
                .subject(subject)
                .claim("tokenVersion", version)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .build();
    }
}
