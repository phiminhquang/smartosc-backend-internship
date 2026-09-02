package com.example.device.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreationRequest {

    @NotBlank(message = "INVALID_ROLE_NAME")
    private String name;

    private String description;
}