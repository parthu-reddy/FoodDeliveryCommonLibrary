package com.fooddelivery.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler defaultErrorHandler() {
        // Retry up to 3 times with a 2-second delay between attempts
        FixedBackOff fixedBackOff = new FixedBackOff(2000L, 3);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (consumerRecord, exception) -> {
                    log.error("Kafka Message failed after maximum retries. Topic: {}, Key: {}, Error: {}",
                            consumerRecord.topic(), consumerRecord.key(), exception.getMessage());
                    // Here we could publish to a central DLQ topic if desired.
                },
                fixedBackOff
        );

        // Optional: Do not retry for specific fatal exceptions
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        
        return errorHandler;
    }
}
