package com.example.device.service.impl;

import com.example.device.enums.EmailJobStatus;
import com.example.device.model.EmailJob;
import com.example.device.repository.EmailJobRepository;
import com.example.device.service.EmailJobClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailJobClaimServiceImpl implements EmailJobClaimService {

    private static final int MAX_BATCH_SIZE = 100;
    private static final int MAX_LEASE_OWNER_LENGTH = 128;

    private final EmailJobRepository emailJobRepository;

    @Override
    @Transactional
    public List<UUID> claimBatch(
            String leaseOwner,
            Instant now,
            Duration leaseDuration,
            int batchSize
    ) {
        validateArguments(leaseOwner, now, leaseDuration, batchSize);

        List<EmailJob> jobs = emailJobRepository.findClaimableForUpdate(now, batchSize);
        Instant leaseUntil = now.plus(leaseDuration);

        for (EmailJob job : jobs) {
            job.setStatus(EmailJobStatus.PROCESSING);
            job.setAttemptCount(Math.addExact(job.getAttemptCount(), 1));
            job.setLeaseOwner(leaseOwner);
            job.setLeaseUntil(leaseUntil);
            job.setUpdatedAt(now);
        }

        emailJobRepository.flush();
        return jobs.stream().map(EmailJob::getId).toList();
    }

    private void validateArguments(
            String leaseOwner,
            Instant now,
            Duration leaseDuration,
            int batchSize
    ) {
        if (leaseOwner == null || leaseOwner.isBlank()
                || leaseOwner.length() > MAX_LEASE_OWNER_LENGTH) {
            throw new IllegalArgumentException("leaseOwner must contain 1 to 128 characters");
        }
        if (now == null) {
            throw new IllegalArgumentException("now is required");
        }
        if (leaseDuration == null || leaseDuration.isZero() || leaseDuration.isNegative()) {
            throw new IllegalArgumentException("leaseDuration must be positive");
        }
        if (batchSize < 1 || batchSize > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("batchSize must be between 1 and 100");
        }
    }
}
