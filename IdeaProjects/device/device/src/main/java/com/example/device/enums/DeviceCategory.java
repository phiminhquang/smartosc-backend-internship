package com.example.device.enums;

import lombok.Getter;

@Getter
public enum DeviceCategory {

    LAPTOP("L"),
    MONITOR("M"),
    PHONE("P");

    private final String code;

    DeviceCategory(String code) {
        this.code = code;
    }
}
