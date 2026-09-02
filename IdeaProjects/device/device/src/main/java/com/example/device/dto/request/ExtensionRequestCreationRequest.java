package com.example.device.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ExtensionRequestCreationRequest {

    @NotNull
    @Future
    private LocalDateTime requestedReturnAt;

    @NotBlank
    private String reason;
}