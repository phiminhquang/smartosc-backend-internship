package com.example.device.service.impl;

import com.example.device.dto.request.IntrospectRequest;
import com.example.device.model.User;
import com.example.device.repository.UserRepository;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationimplTest {

    private static final String SIGNER_KEY =
            "0123456789012345678901234567890123456789012345678901234567890123";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private Authenticationimpl authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new Authenticationimpl(userRepository, passwordEncoder);
        ReflectionTestUtils.setField(authenticationService, "signerKey", SIGNER_KEY);
    }

    @Test
    void introspect_currentTokenVersion_shouldBeValid() throws Exception {
        User user = User.builder().email("user@example.com").tokenVersion(3).build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertTrue(authenticationService.introspect(request(signedToken(user.getEmail(), 3))).isValid());
    }

    @Test
    void introspect_oldTokenVersion_shouldBeInvalid() throws Exception {
        User user = User.builder().email("user@example.com").tokenVersion(4).build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertFalse(authenticationService.introspect(request(signedToken(user.getEmail(), 3))).isValid());
    }

    private String signedToken(String email, long tokenVersion) throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(email)
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .claim("tokenVersion", tokenVersion)
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
        jwt.sign(new MACSigner(SIGNER_KEY));
        return jwt.serialize();
    }

    private IntrospectRequest request(String token) {
        IntrospectRequest request = new IntrospectRequest();
        request.setToken(token);
        return request;
    }
}
