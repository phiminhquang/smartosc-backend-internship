package com.example.device.service.impl;

import com.example.device.repository.RateLimitBucketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RateLimitCounterStore {

    private final RateLimitBucketRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int incrementAndGet(byte[] bucketKey, Instant now, Instant expiresAt) {
        return repository.incrementAndGet(bucketKey, now, expiresAt);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deleteExpired(Instant now, int batchSize) {
        return repository.deleteExpired(now, batchSize);
    }
}
