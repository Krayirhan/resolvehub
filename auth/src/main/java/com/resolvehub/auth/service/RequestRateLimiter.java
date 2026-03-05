package com.resolvehub.auth.service;

public interface RequestRateLimiter {
    boolean allow(String key, int limitPerMinute);
}
