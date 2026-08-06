package com.fooddelivery.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class KafkaListenerMdcAspect {

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public Object wrapWithMdc(ProceedingJoinPoint pjp) throws Throwable {
        String traceId = MDC.get("traceId");
        boolean isNewTrace = false;
        
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
            MDC.put("traceId", traceId);
            isNewTrace = true;
        }
        
        try {
            return pjp.proceed();
        } finally {
            if (isNewTrace) {
                MDC.clear();
            }
        }
    }
}
