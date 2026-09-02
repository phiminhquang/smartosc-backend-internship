package com.example.device.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceAssignmentRequest {

    @NotNull(message = "INVALID_USER_ID")
    private UUID userId;

    @NotNull(message = "INVALID_DEVICE_ID")
    private UUID deviceId;

    @NotNull(message = "INVALID_RETURN_DATE")
    @Future(message = "INVALID_RETURN_DATE")
    private LocalDateTime expectedReturnAt;
}