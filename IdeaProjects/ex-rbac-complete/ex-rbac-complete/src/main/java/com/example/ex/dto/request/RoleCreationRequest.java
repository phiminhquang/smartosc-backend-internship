package com.example.ex.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleCreationRequest {

    @NotBlank(message = "ROLE_NAME_INVALID")
    @Size(max = 50, message = "ROLE_NAME_INVALID")
    private String name;

    private String description;
}
