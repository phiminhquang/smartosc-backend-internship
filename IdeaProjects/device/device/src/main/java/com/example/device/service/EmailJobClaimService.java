package com.example.device.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EmailJobClaimService {

    List<UUID> claimBatch(
            String leaseOwner,
            Instant now,
            Duration leaseDuration,
            int batchSize
    );
}
