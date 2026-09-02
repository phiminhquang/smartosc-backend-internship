package com.example.device.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairUnrepairableRequest {

    @NotBlank(message = "INVALID_REPAIR_NOTE")
    private String repairNote;
}