package com.fooddelivery.common.lock;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Supplies {@link RedisLock} where a {@link StringRedisTemplate} exists.
 *
 * <p>{@code RedisLock} was an unconditional {@code @Component}, so every service scanning
 * {@code com.fooddelivery.common} had to provide a {@code StringRedisTemplate} or fail to start —
 * including services that never take a distributed lock, and including test profiles that
 * deliberately exclude {@code RedisAutoConfiguration}. CampaignService's context tests failed for
 * exactly that reason.
 */
@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(StringRedisTemplate.class)
public class RedisSupportConfiguration {

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    @ConditionalOnMissingBean
    public RedisLock redisLock(StringRedisTemplate redisTemplate) {
        return new RedisLock(redisTemplate);
    }
}
