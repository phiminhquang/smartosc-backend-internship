package com.example.device.controller;

import com.example.device.configuration.JwtTokenVersionValidator;
import com.example.device.configuration.SecurityConfig;
import com.example.device.configuration.ApiCorsConfiguration;
import com.example.device.observability.RequestCorrelation;
import com.example.device.observability.RequestCorrelationFilter;
import com.example.device.service.AuthenticationService;
import com.example.device.service.PasswordResetService;
import com.example.device.service.PasswordResetTimingGuard;
import com.example.device.service.RateLimitService;
import com.example.device.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@WebMvcTest(AuthenticationController.class)
@Import({SecurityConfig.class, ApiCorsConfiguration.class, RequestCorrelationFilter.class})
@TestPropertySource(properties = {
        "jwt.signer-key=0123456789012345678901234567890123456789012345678901234567890123",
        "app.cors.allowed-origins=https://device-demo.onrender.com"
})
class AuthenticationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @MockitoBean
    private RateLimitService rateLimitService;

    @MockitoBean
    private PasswordResetTimingGuard passwordResetTimingGuard;

    @MockitoBean
    private JwtTokenVersionValidator tokenVersionValidator;

    @Test
    void passwordResetRequest_isPublicAndReturnsSameResponseForEveryEmail() throws Exception {
        String existingResponse = mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"existing@example.com\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.code").value(1000))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String unknownResponse = mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"unknown@example.com\"}"))
                .andExpect(status().isAccepted())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(existingResponse, unknownResponse);
    }

    @Test
    void passwordResetConfirm_isPublic() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "0123456789012345678901234567890123456789012",
                                  "newPassword": "new-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }

    @Test
    void passwordResetRequest_whenRateLimited_returns429AndRetryAfterWithoutAccountLookup() throws Exception {
        doThrow(new RateLimitExceededException(42))
                .when(rateLimitService)
                .checkPasswordResetRequest(anyString(), anyString());

        mockMvc.perform(post("/api/auth/password-reset/request")
                        .header(RequestCorrelation.HEADER_NAME, "request-rate-limit-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"unknown@example.com\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "42"))
                .andExpect(header().string(RequestCorrelation.HEADER_NAME, "request-rate-limit-123"))
                .andExpect(jsonPath("$.code").value(1057))
                .andExpect(jsonPath("$.requestId").value("request-rate-limit-123"));

        verify(passwordResetService, never()).requestReset(anyString());
        verify(passwordResetTimingGuard, never()).awaitNeutralResponseWindow(anyLong());
    }

    @Test
    void login_whenRateLimited_returns429BeforeAuthentication() throws Exception {
        doThrow(new RateLimitExceededException(30))
                .when(rateLimitService)
                .checkLogin(anyString(), anyString());

        mockMvc.perform(post("/api/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "30"))
                .andExpect(jsonPath("$.code").value(1057));

        verify(authenticationService, never()).authenticate(any());
    }

    @Test
    void otherEndpoints_stillRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void allowedFrontend_canPreflightProtectedApiWithoutJwt() throws Exception {
        mockMvc.perform(options("/api/users")
                        .header("Origin", "https://device-demo.onrender.com")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization,x-request-id"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://device-demo.onrender.com"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }

    @Test
    void untrustedFrontend_isRejectedBeforePasswordReset() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset/request")
                        .header("Origin", "https://device-demo.onrender.com.attacker.example")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"unknown@example.com\"}"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));

        verify(rateLimitService, never()).checkPasswordResetRequest(anyString(), anyString());
        verify(passwordResetService, never()).requestReset(anyString());
    }

    @Test
    void allowedFrontend_stillNeedsJwtForProtectedApi() throws Exception {
        mockMvc.perform(get("/api/users").header("Origin", "https://device-demo.onrender.com"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://device-demo.onrender.com"));
    }

    @Test
    void allowedFrontend_canReadCorrelationAndRetryHeadersOnErrors() throws Exception {
        doThrow(new RateLimitExceededException(30))
                .when(rateLimitService).checkLogin(anyString(), anyString());

        mockMvc.perform(post("/api/auth/token")
                        .header("Origin", "https://device-demo.onrender.com")
                        .header(RequestCorrelation.HEADER_NAME, "demo-cors-error")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://device-demo.onrender.com"))
                .andExpect(header().string("Access-Control-Expose-Headers", "X-Request-ID, Retry-After"))
                .andExpect(header().string("Retry-After", "30"))
                .andExpect(jsonPath("$.requestId").value("demo-cors-error"));
    }
}
