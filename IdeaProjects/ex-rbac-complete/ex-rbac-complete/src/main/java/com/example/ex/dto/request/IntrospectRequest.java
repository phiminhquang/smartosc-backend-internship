package com.example.ex.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntrospectRequest {

    @NotBlank(message = "TOKEN_INVALID")
    private String token;
}
