package com.example.device.service;

import com.example.device.dto.request.AuthenticationRequest;
import com.example.device.dto.request.IntrospectRequest;
import com.example.device.dto.response.AuthenticationResponse;
import com.example.device.dto.response.IntrospectResponse;
import org.springframework.stereotype.Service;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);
    IntrospectResponse introspect(IntrospectRequest request);
}
