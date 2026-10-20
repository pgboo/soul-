package com.soul.social.client.impl;

import com.soul.social.client.CacheService;
import jakarta.annotation.PostConstruct;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 内存缓存实现（默认启用）。ConcurrentHashMap + 惰性过期 + 定时清理。
 * <p>零 Redis 可启动的核心支撑。
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "soul.cache.type", havingValue = "local", matchIfMissing = true)
public class LocalCacheServiceImpl implements CacheService {

    /** 缓存项：值 + 过期时间(epochMs，<=0 表示永不过期) */
    private record CacheEntry(String value, long expireAt) {
        boolean expired(long now) {
            return expireAt > 0 && now >= expireAt;
        }
    }

    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("LocalCacheServiceImpl 已启用（内存缓存，零 Redis 依赖）");
    }

    @Override
    public void set(String key, String value, long ttlSeconds) {
        if (key == null) {
            return;
        }
        long expireAt = ttlSeconds > 0 ? System.currentTimeMillis() + ttlSeconds * 1000L : 0L;
        store.put(key, new CacheEntry(value, expireAt));
    }

    @Override
    public String get(String key) {
        if (key == null) {
            return null;
        }
        CacheEntry entry = store.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expired(System.currentTimeMillis())) {
            store.remove(key);
            return null;
        }
        return entry.value();
    }

    @Override
    public boolean delete(String key) {
        if (key == null) {
            return false;
        }
        return store.remove(key) != null;
    }

    @Override
    public boolean hasKey(String key) {
        return get(key) != null;
    }

    @Override
    public synchronized long increment(String key, long delta) {
        if (key == null) {
            return 0L;
        }
        long now = System.currentTimeMillis();
        CacheEntry entry = store.get(key);
        long current = 0L;
        long expireAt = 0L;
        if (entry != null && !entry.expired(now)) {
            try {
                current = Long.parseLong(entry.value());
            } catch (NumberFormatException e) {
                current = 0L;
            }
            expireAt = entry.expireAt();
        }
        long next = current + delta;
        store.put(key, new CacheEntry(String.valueOf(next), expireAt));
        return next;
    }

    @Override
    public boolean expire(String key, long ttlSeconds) {
        if (key == null) {
            return false;
        }
        CacheEntry entry = store.get(key);
        if (entry == null || entry.expired(System.currentTimeMillis())) {
            return false;
        }
        long expireAt = ttlSeconds > 0 ? System.currentTimeMillis() + ttlSeconds * 1000L : 0L;
        store.put(key, new CacheEntry(entry.value(), expireAt));
        return true;
    }

    /**
     * 定时清理过期 key（每 60 秒）。
     */
    @Scheduled(fixedDelay = 60000)
    public void cleanExpired() {
        long now = System.currentTimeMillis();
        int removed = 0;
        Iterator<Map.Entry<String, CacheEntry>> it = store.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, CacheEntry> e = it.next();
            if (e.getValue().expired(now)) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("LocalCache 清理过期 key {} 个，剩余 {} 个", removed, store.size());
        }
    }
}
