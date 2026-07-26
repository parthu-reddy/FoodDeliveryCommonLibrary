package com.fooddelivery.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
@Slf4j
public class KafkaConfig {

    @Bean
    @Primary
    public DefaultErrorHandler defaultErrorHandler(KafkaOperations<Object, Object> kafkaOperations) {
        // Standardized Exponential Backoff: Initial interval 1000ms, multiplier 2.0, max 3 retries (max interval 10000ms)
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10000L);
        
        // Recoverer that sends the failed message to a DLT topic (original topic name + ".DLT")
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaOperations,
                (consumerRecord, exception) -> {
                    log.error("Kafka Message failed after maximum retries. Routing to DLT. Topic: {}, Key: {}, Error: {}",
                            consumerRecord.topic(), consumerRecord.key(), exception.getMessage());
                    return new org.apache.kafka.common.TopicPartition(consumerRecord.topic() + ".DLT", consumerRecord.partition());
                });

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // Optional: Do not retry for specific fatal exceptions
        errorHandler.addNotRetryableExceptions(
            IllegalArgumentException.class,
            com.fasterxml.jackson.core.JsonProcessingException.class
        );
        
        return errorHandler;
    }
}
