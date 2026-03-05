package com.resolvehub.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RedisBackedRateLimiter implements RequestRateLimiter {
    private static final Logger log = LoggerFactory.getLogger(RedisBackedRateLimiter.class);
    private static final DateTimeFormatter WINDOW_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(ZoneOffset.UTC);

    @Nullable
    private final StringRedisTemplate redisTemplate;
    private final Map<String, AtomicInteger> fallbackCounters = new ConcurrentHashMap<>();

    public RedisBackedRateLimiter(@Nullable StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean allow(String key, int limitPerMinute) {
        String window = WINDOW_FORMATTER.format(Instant.now());
        String finalKey = "rl:" + window + ":" + key;
        if (redisTemplate != null) {
            try {
                Long count = redisTemplate.opsForValue().increment(finalKey);
                if (count != null && count == 1L) {
                    redisTemplate.expire(finalKey, Duration.ofMinutes(2));
                }
                return count != null && count <= limitPerMinute;
            } catch (Exception ex) {
                log.warn("Redis unavailable for rate limiting, switching to in-memory fallback: {}", ex.getMessage());
            }
        }
        int localCount = fallbackCounters.computeIfAbsent(finalKey, ignored -> new AtomicInteger()).incrementAndGet();
        return localCount <= limitPerMinute;
    }
}
