package com.fooddelivery.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class KafkaMdcAspect {

    private static final String TRACE_ID_MDC_KEY = "traceId";

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public Object injectMdcForKafkaListener(ProceedingJoinPoint joinPoint) throws Throwable {
        String existingTraceId = MDC.get(TRACE_ID_MDC_KEY);
        boolean isNewTrace = false;

        if (existingTraceId == null || existingTraceId.isEmpty()) {
            MDC.put(TRACE_ID_MDC_KEY, UUID.randomUUID().toString());
            isNewTrace = true;
        }

        try {
            return joinPoint.proceed();
        } finally {
            if (isNewTrace) {
                MDC.remove(TRACE_ID_MDC_KEY);
            }
        }
    }
}
