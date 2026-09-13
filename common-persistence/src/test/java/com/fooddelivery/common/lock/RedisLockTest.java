package com.fooddelivery.common.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import java.time.Duration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisLockTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisLock redisLock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        redisLock = new RedisLock(redisTemplate);
    }

    @Test
    void testTryAcquireSuccess() {
        String key = "testKey";
        String token = "testToken";
        Duration duration = Duration.ofSeconds(10);
        
        when(valueOperations.setIfAbsent(key, token, duration)).thenReturn(true);
        
        boolean acquired = redisLock.tryAcquire(key, token, duration);
        assertTrue(acquired);
    }

    @Test
    void testTryAcquireFailure() {
        String key = "testKey";
        String token = "testToken";
        Duration duration = Duration.ofSeconds(10);
        
        when(valueOperations.setIfAbsent(key, token, duration)).thenReturn(false);
        
        boolean acquired = redisLock.tryAcquire(key, token, duration);
        assertFalse(acquired);
    }

    @Test
    void testReleaseSuccess() {
        String key = "testKey";
        String token = "testToken";
        
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList(key)), eq(token)))
            .thenReturn(1L);
            
        boolean released = redisLock.release(key, token);
        assertTrue(released);
    }

    @Test
    void testReleaseWithStaleTokenFails() {
        String key = "testKey";
        String token = "staleToken";
        
        // Simulating the Lua script returning 0 when the token does not match
        when(redisTemplate.execute(any(RedisScript.class), eq(Collections.singletonList(key)), eq(token)))
            .thenReturn(0L);
            
        boolean released = redisLock.release(key, token);
        
        // Assert that the release failed, preventing the deletion of someone else's lock
        assertFalse(released);
    }
}
