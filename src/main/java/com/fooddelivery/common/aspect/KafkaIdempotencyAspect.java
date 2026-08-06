package com.fooddelivery.common.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaIdempotencyAspect {

    private final StringRedisTemplate redisTemplate;
    
    // We assume the first argument is a JSON string or some object where we can extract a unique ID.
    // If it's an object, we can try to call getId() or toString() but for Kafka messages, it's usually better to check headers.
    // Since intercepting raw Kafka headers in AOP is tricky if the method signature varies, 
    // a simpler approach for @KafkaListener is intercepting the payload and hashing it, 
    // or if the payload has a unique eventId, extracting it.
    
    // For now, let's just use the string representation of the payload as a deduplication hash if it's not too large.
    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public Object checkIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return joinPoint.proceed();
        }
        
        Object payload = args[0];
        String deduplicationKey = "kafka_idempotency:" + joinPoint.getSignature().toShortString() + ":" + payload.hashCode();
        
        Boolean isNewMessage = redisTemplate.opsForValue().setIfAbsent(deduplicationKey, "processed", Duration.ofHours(24));
        
        if (Boolean.TRUE.equals(isNewMessage)) {
            try {
                return joinPoint.proceed();
            } catch (Throwable t) {
                // If processing fails, we must delete the key so it can be retried by the consumer
                redisTemplate.delete(deduplicationKey);
                throw t;
            }
        } else {
            log.warn("Idempotency hit! Duplicate Kafka message detected for key: {}. Skipping processing.", deduplicationKey);
            return null; // Skip processing
        }
    }
}
