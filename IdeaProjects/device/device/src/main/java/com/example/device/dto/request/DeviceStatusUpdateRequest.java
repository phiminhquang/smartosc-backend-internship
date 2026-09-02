package com.example.device.dto.request;

import com.example.device.enums.DeviceState;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatusUpdateRequest {

    @NotNull(message = "INVALID_DEVICE_STATE")
    private DeviceState state;
}