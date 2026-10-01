package com.example.device.controller;

import com.example.device.configuration.JwtTokenVersionValidator;
import com.example.device.configuration.SecurityConfig;
import com.example.device.service.AuthenticationService;
import com.example.device.service.PasswordResetService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "jwt.signer-key=0123456789012345678901234567890123456789012345678901234567890123")
class AuthenticationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private PasswordResetService passwordResetService;

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
    void otherEndpoints_stillRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}
