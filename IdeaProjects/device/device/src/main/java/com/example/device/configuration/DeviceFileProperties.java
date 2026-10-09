package com.example.device.configuration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.device-file")
public class DeviceFileProperties {

    @Min(1)
    @Max(10_000)
    private int exportBatchSize = 500;

    @Min(1)
    @Max(10_000)
    private int importBatchSize = 100;

    @Min(1)
    @Max(10_000)
    private int excelRowWindow = 100;

    @Min(1)
    private long maxImportBytes = 10L * 1024 * 1024;
}
