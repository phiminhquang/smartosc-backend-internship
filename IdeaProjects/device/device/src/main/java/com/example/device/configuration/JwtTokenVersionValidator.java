package com.example.device.configuration;

import com.example.device.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenVersionValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_TOKEN = new OAuth2Error(
            "invalid_token",
            "The token is no longer valid",
            null
    );

    private final UserRepository userRepository;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        Number claimedVersion = jwt.getClaim("tokenVersion");

        if (jwt.getSubject() == null || claimedVersion == null) {
            return OAuth2TokenValidatorResult.failure(INVALID_TOKEN);
        }

        return userRepository.findByEmail(jwt.getSubject())
                .filter(user -> user.getTokenVersion() == claimedVersion.longValue())
                .map(user -> OAuth2TokenValidatorResult.success())
                .orElseGet(() -> OAuth2TokenValidatorResult.failure(INVALID_TOKEN));
    }
}
