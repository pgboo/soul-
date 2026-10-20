package com.soul.social.client.impl;

import cn.hutool.core.util.StrUtil;
import com.soul.social.client.CacheService;
import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis 缓存实现（骨架，条件装配默认关闭）。
 * <p>仅当 {@code soul.cache.type=redis} 时启用；本机无 Redis，默认不装配。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "soul.cache.type", havingValue = "redis")
public class RedisCacheServiceImpl implements CacheService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void set(String key, String value, long ttlSeconds) {
        if (ttlSeconds > 0) {
            stringRedisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        } else {
            stringRedisTemplate.opsForValue().set(key, value);
        }
    }

    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }

    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    @Override
    public long increment(String key, long delta) {
        Long value = stringRedisTemplate.opsForValue().increment(key, delta);
        return value == null ? 0L : value;
    }

    @Override
    public boolean expire(String key, long ttlSeconds) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS));
    }
}
