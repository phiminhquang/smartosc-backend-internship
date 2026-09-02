package com.example.ex.service.impl;

import com.example.ex.dto.request.AuthenticationRequest;
import com.example.ex.dto.request.IntrospectRequest;
import com.example.ex.dto.response.AuthenticationResponse;
import com.example.ex.dto.response.IntrospectResponse;
import com.example.ex.exception.AppException;
import com.example.ex.exception.ErrorCode;
import com.example.ex.model.Roles;
import com.example.ex.model.User;
import com.example.ex.repository.UserRepository;
import com.example.ex.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.signer-key}")
    private String signerKey;

    @Value("${jwt.expiration-seconds:3600}")
    private long expirationSeconds;

    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        boolean passwordCorrect = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!passwordCorrect) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        Instant expiryTime = Instant.now().plusSeconds(expirationSeconds);
        String token = generateToken(user,expiryTime);

        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .expiryTime(expiryTime)
                .build();
    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request) {
        boolean valid;

        try {
            valid = verifyToken(request.getToken());
        } catch (JOSEException | ParseException e) {
            valid = false;
        }

        return IntrospectResponse.builder()
                .valid(valid)
                .build();
    }
    //cái method này để cấu hình jwt sẽ chứa những nội dung gì như header + payload +  signature
    private String generateToken(User user,Instant expiryTime) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
//payload
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("ex-rbac-service")
                .issueTime(new Date())
                .expirationTime(Date.from(expiryTime))
                .claim("scope", buildScope(user))
                .build();
//nối header và payload
        SignedJWT signedJWT = new SignedJWT(header, claims);
// biên signerKey từ string sang byte và dùng đẻ tạo signature nối vào tạo thành 1 jwt hoàn chỉnh
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
    //kiểm tra token đúng chưa
    private boolean verifyToken(String token)
            throws JOSEException, ParseException {

        SignedJWT signedJWT = SignedJWT.parse(token);

        JWSVerifier verifier = new MACVerifier(
                signerKey.getBytes(StandardCharsets.UTF_8)
        );
// verify() tự tính lại signature bằng signerKey
// rồi so với signature có sẵn bên trong signedJWT.
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

        for (Roles role : user.getRoles()) {
            scope.add(role.getName());
        }

        return scope.toString();
    }
}