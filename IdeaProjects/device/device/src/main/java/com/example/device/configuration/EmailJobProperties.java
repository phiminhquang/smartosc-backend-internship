package com.example.device.configuration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.email-job")
public class EmailJobProperties {

    private boolean workerEnabled;

    @Min(1)
    @Max(100)
    private int batchSize = 20;

    @Min(1)
    @Max(20)
    private int workerThreads = 20;

    @NotNull
    private Duration leaseDuration = Duration.ofSeconds(60);

    @NotNull
    private Duration completedRetention = Duration.ofDays(30);

    @NotNull
    private Duration failedRetention = Duration.ofDays(90);
}
