package com.muluken.jobtracker.common.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(
                10,
                Refill.intervally(10, Duration.ofHours(1))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    public Bucket getBucket(String email) {
        return buckets.computeIfAbsent(email, k -> createBucket());
    }

    public boolean tryConsume(String email) {
        return getBucket(email).tryConsume(1);
    }

    public long getRemainingTokens(String email) {
        return getBucket(email).getAvailableTokens();
    }
}