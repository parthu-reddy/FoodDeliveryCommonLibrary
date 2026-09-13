package com.fooddelivery.common.constants;

public final class LuaScripts {
    private LuaScripts() {}
    
    // Token Bucket Rate Limiter Script
    // KEYS[1] = bucket key
    // ARGV[1] = rate (tokens per second)
    // ARGV[2] = capacity
    // ARGV[3] = now (timestamp)
    // ARGV[4] = requested tokens (usually 1)
    public static final String RATE_LIMITER_SCRIPT =
        "local key = KEYS[1] " +
        "local rate = tonumber(ARGV[1]) " +
        "local capacity = tonumber(ARGV[2]) " +
        "local now = tonumber(ARGV[3]) " +
        "local requested = tonumber(ARGV[4]) " +
        "local fill_time = capacity / rate " +
        "local ttl = math.floor(fill_time * 2) " +
        "local last_tokens = tonumber(redis.call('HGET', key, 'tokens')) " +
        "if last_tokens == nil then " +
        "  last_tokens = capacity " +
        "end " +
        "local last_refreshed = tonumber(redis.call('HGET', key, 'timestamp')) " +
        "if last_refreshed == nil then " +
        "  last_refreshed = 0 " +
        "end " +
        "local delta = math.max(0, now - last_refreshed) " +
        "local filled_tokens = math.min(capacity, last_tokens + (delta * rate)) " +
        "local allowed = filled_tokens >= requested " +
        "local new_tokens = filled_tokens " +
        "if allowed then " +
        "  new_tokens = filled_tokens - requested " +
        "end " +
        "redis.call('HSET', key, 'tokens', new_tokens) " +
        "redis.call('HSET', key, 'timestamp', now) " +
        "redis.call('EXPIRE', key, ttl) " +
        "return { allowed and 1 or 0, new_tokens }";

    // Compare and Delete lock release script
    // KEYS[1] = lock key
    // ARGV[1] = lock token
    public static final String RELEASE_LOCK_SCRIPT =
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "return redis.call('del', KEYS[1]) " +
        "else " +
        "return 0 " +
        "end";
}
