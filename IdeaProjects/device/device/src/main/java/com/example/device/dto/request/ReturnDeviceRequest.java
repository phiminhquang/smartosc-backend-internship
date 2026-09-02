package com.example.device.dto.request;

import com.example.device.enums.DeviceReturnCondition;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReturnDeviceRequest {

    @NotNull
    private DeviceReturnCondition condition;

    private String note;
}