package com.example.device.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailJobStateService {

    Optional<EmailJobDelivery> loadForDelivery(UUID id, String leaseOwner, Instant now);

    boolean markSent(UUID id, String leaseOwner, Instant now);

    boolean markFailed(
            UUID id,
            String leaseOwner,
            Instant now,
            boolean permanent,
            String errorCode,
            String errorSummary
    );

    int recoverExpiredLeases(Instant now, int batchSize);

    int cleanup(Instant now, int batchSize);
}
