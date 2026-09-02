package com.example.device.dto.response;

import com.example.device.enums.DeviceCategory;
import com.example.device.enums.DeviceState;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {

    private String id;
    private DeviceCategory category;
    private String serialNumber;
    private String name;
    private String model;
    private String description;
    private DeviceState state;
    private String updatedBy;
    private LocalDateTime updatedTime;
}