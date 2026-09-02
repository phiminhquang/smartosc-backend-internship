package com.example.device.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class IntrospectRequest {

    @NotBlank
    private String token;
}
