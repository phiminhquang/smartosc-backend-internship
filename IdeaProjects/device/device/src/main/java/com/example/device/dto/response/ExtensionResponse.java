package com.example.device.dto.response;

import com.example.device.enums.ExtensionRequestStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtensionResponse {

    private UUID id;

    private UUID assignmentId;

    private UUID deviceId;
    private String deviceName;
    private String deviceSerialNumber;

    private LocalDateTime previousReturnAt;
    private LocalDateTime requestedReturnAt;

    private String reason;
    private ExtensionRequestStatus status;

    private String requestedBy;
    private LocalDateTime requestedAt;

    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String reviewNote;
}