package com.example.device.service;

import com.example.device.enums.DeviceCategory;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class DeviceSerialNumberGenerator {

    private static final int DODAI = 7;
    private static final String CHARACTER = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private int index;
    private SecureRandom random = new SecureRandom();
    public String genarate(DeviceCategory category) {
        StringBuilder a = new StringBuilder();
        a.append(category.getCode());
        for (int i = 0; i < DODAI; i++) {
            index = random.nextInt(CHARACTER.length());
            a.append(CHARACTER.charAt(index));
        }
        return a.toString();
    }
}