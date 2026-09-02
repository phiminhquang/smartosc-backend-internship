package com.example.device.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairCreationRequest {

    @NotNull(message = "INVALID_DEVICE_ID")
    private UUID deviceId;

    @NotBlank(message = "INVALID_REPAIR_ISSUE")
    private String issueDescription;
}