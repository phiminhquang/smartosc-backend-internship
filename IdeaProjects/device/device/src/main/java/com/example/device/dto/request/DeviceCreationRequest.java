package com.example.device.dto.request;

import com.example.device.enums.DeviceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCreationRequest {

    @NotNull(message = "INVALID_DEVICE_CATEGORY")
    private DeviceCategory category;

    @NotBlank(message = "INVALID_DEVICE_NAME")
    private String name;

    @NotBlank(message = "INVALID_DEVICE_MODEL")
    private String model;

    private String description;
}