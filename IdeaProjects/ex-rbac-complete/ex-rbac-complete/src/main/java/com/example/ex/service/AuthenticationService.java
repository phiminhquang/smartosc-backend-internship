package com.example.ex.service;

import com.example.ex.dto.request.AuthenticationRequest;
import com.example.ex.dto.request.IntrospectRequest;
import com.example.ex.dto.response.AuthenticationResponse;
import com.example.ex.dto.response.IntrospectResponse;

public interface AuthenticationService {

    AuthenticationResponse authenticate(AuthenticationRequest request);

    IntrospectResponse introspect(IntrospectRequest request);
}