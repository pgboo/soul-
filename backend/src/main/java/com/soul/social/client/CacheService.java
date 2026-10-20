package com.soul.social.client;

/**
 * 缓存抽象。默认 {@code LocalCacheServiceImpl}(内存) 装配；Redis 实现条件装配、默认关闭。
 */
public interface CacheService {

    /** 写入并设置过期（秒）。ttlSeconds<=0 视为不过期。 */
    void set(String key, String value, long ttlSeconds);

    /** 读取，不存在或已过期返回 null。 */
    String get(String key);

    /** 删除，返回是否存在。 */
    boolean delete(String key);

    /** 是否存在且未过期。 */
    boolean hasKey(String key);

    /** 原子自增，返回自增后的值。key 不存在或非数值时按 0 起始。 */
    long increment(String key, long delta);

    /** 重置过期时间，返回是否成功。 */
    boolean expire(String key, long ttlSeconds);
}
