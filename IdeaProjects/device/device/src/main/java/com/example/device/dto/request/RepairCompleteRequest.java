package com.example.device.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairCompleteRequest {

    @NotBlank(message = "INVALID_REPAIR_NOTE")
    private String repairNote;

    @DecimalMin(value = "0.0", message = "INVALID_REPAIR_COST")
    private BigDecimal cost;
}