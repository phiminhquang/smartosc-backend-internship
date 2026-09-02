package com.example.device.dto.response;

import com.example.device.enums.RepairStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairResponse {

    private UUID id;

    private UUID deviceId;
    private String deviceName;
    private String deviceSerialNumber;

    private String issueDescription;
    private RepairStatus status;

    private String createdBy;
    private LocalDateTime createdAt;

    private String startedBy;
    private LocalDateTime startedAt;

    private String finishedBy;
    private LocalDateTime finishedAt;

    private String repairNote;
    private BigDecimal cost;
}