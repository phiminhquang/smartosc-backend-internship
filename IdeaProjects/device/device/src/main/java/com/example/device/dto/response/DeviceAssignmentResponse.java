package com.example.device.dto.response;

import com.example.device.enums.DeviceAssignmentStatus;
import com.example.device.enums.DeviceReturnCondition;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceAssignmentResponse {

    private UUID id;

    private UUID userId;
    private String userName;
    private String userEmail;

    private UUID deviceId;
    private String deviceName;
    private String deviceSerialNumber;

    private String assignedBy;
    private LocalDateTime assignedAt;
    private LocalDateTime expectedReturnAt;
    private LocalDateTime returnedAt;

    private DeviceAssignmentStatus status;

    private DeviceReturnCondition returnCondition;
    private String returnNote;

    private LocalDateTime reminderNotifiedAt;
    private LocalDateTime overdueNotifiedAt;
}