package com.example.device.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
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
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    @Valid
    @NotNull
    private Policy login = new Policy(20, 5, Duration.ofMinutes(5));

    @Valid
    @NotNull
    private Policy passwordReset = new Policy(10, 3, Duration.ofMinutes(15));

    @Min(1)
    @Max(10_000)
    private int cleanupBatchSize = 1_000;

    @Getter
    @Setter
    public static class Policy {

        @Min(1)
        @Max(1_000_000)
        private int ipLimit;

        @Min(1)
        @Max(1_000_000)
        private int identityLimit;

        @NotNull
        private Duration window;

        public Policy() {
        }

        public Policy(int ipLimit, int identityLimit, Duration window) {
            this.ipLimit = ipLimit;
            this.identityLimit = identityLimit;
            this.window = window;
        }

        @AssertTrue(message = "rate-limit window must be at least one second")
        public boolean isWindowValid() {
            return window != null && !window.isNegative() && window.getSeconds() >= 1;
        }
    }
}
