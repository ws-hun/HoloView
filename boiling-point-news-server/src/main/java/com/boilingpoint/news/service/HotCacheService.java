package com.boilingpoint.news.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.cache.enabled:true}")
    private boolean enabled;

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (!enabled) {
            return null;
        }
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (RuntimeException exception) {
            log.warn("Redis cache read failed: key={}, error={}", key, exception.toString());
            return null;
        }
    }

    public void put(String key, Object value, Duration ttl) {
        if (!enabled) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (RuntimeException exception) {
            log.warn("Redis cache write failed: key={}, error={}", key, exception.toString());
        }
    }

    public void evictByPrefix(String prefix) {
        if (!enabled) {
            return;
        }
        try {
            var keys = redisTemplate.keys(prefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Redis cache evicted: prefix={}, count={}", prefix, keys.size());
            }
        } catch (RuntimeException exception) {
            log.warn("Redis cache eviction failed: prefix={}, error={}", prefix, exception.toString());
        }
    }
}
