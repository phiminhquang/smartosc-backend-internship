package com.example.device.service.impl;

import com.example.device.dto.request.AuthenticationRequest;
import com.example.device.dto.request.IntrospectRequest;
import com.example.device.dto.response.AuthenticationResponse;
import com.example.device.dto.response.IntrospectResponse;
import com.example.device.exception.AppException;
import com.example.device.exception.ErrorCode;
import com.example.device.model.Role;
import com.example.device.model.User;
import com.example.device.repository.UserRepository;
import com.example.device.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor

public class Authenticationimpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.signer-key}")
    private String signerKey;

    @Value("${jwt.expiration-seconds}")
    private long expirationSeconds;

    @Override
    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request){
        User user= userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new AppException(ErrorCode.UNAUTHENTICATED));
        boolean passWord = passwordEncoder.matches(request.getPassword(),user.getPassword());
        if(!passWord){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        Instant time = Instant.now().plusSeconds(expirationSeconds);
        String token = generateToken(user,time);
        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .expiryTime(time)
                .build();
    }
    @Override
    public IntrospectResponse introspect(IntrospectRequest introspectRequest) {
        boolean valid;
        try {
            valid = verifyToken(introspectRequest.getToken());
        }
        catch (JOSEException | ParseException e) {
            valid = false;
        }
        return IntrospectResponse.builder()
                .valid(valid)
                .build();
    }
    private String generateToken(User user,Instant expiryTime) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("ex-rbac-service")
                .issueTime(new Date())
                .expirationTime(Date.from(expiryTime))
                .claim("scope", buildScope(user))
                .build();
        SignedJWT signedJWT = new SignedJWT(header, claims);
        try {
            signedJWT.sign(
                    new MACSigner(
                            signerKey.getBytes(StandardCharsets.UTF_8)
                    )
            );
        } catch (JOSEException e) {
            throw new IllegalStateException("Cannot sign JWT", e);
        }

        return signedJWT.serialize();
    }
    private boolean verifyToken(String token) throws JOSEException, ParseException {

        SignedJWT signedJWT = SignedJWT.parse(token);

        JWSVerifier verifier = new MACVerifier(
                signerKey.getBytes(StandardCharsets.UTF_8)
        );

        boolean signatureValid = signedJWT.verify(verifier);

        Date expirationTime = signedJWT
                .getJWTClaimsSet()
                .getExpirationTime();

        return signatureValid
                && expirationTime != null
                && expirationTime.toInstant().isAfter(Instant.now());
    }

    private String buildScope(User user) {
        StringJoiner scope = new StringJoiner(" ");

        for (Role role : user.getRoles()) {
            scope.add(role.getName());
        }

        return scope.toString();
    }
}

