package com.fooddelivery.common.lock;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

@Component
public class RedisLock {

    private final StringRedisTemplate redisTemplate;
    
    private final DefaultRedisScript<Long> releaseLockScript;

    public RedisLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.releaseLockScript = new DefaultRedisScript<>();
        this.releaseLockScript.setScriptText(com.fooddelivery.common.constants.LuaScripts.RELEASE_LOCK_SCRIPT);
        this.releaseLockScript.setResultType(Long.class);
    }

    /**
     * Tries to acquire a distributed lock.
     * 
     * @param key   The lock key.
     * @param token The lock token (used for safe release).
     * @param ttl   The time to live for the lock.
     * @return true if acquired, false otherwise.
     */
    public boolean tryAcquire(String key, String token, Duration ttl) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, token, ttl);
        return acquired != null && acquired;
    }

    /**
     * Releases a distributed lock using a Lua script to ensure atomicity.
     * 
     * @param key   The lock key.
     * @param token The lock token.
     * @return true if successfully released by the owner, false otherwise.
     */
    public boolean release(String key, String token) {
        Long result = redisTemplate.execute(releaseLockScript, Collections.singletonList(key), token);
        return result != null && result == 1L;
    }
}
