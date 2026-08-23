package com.fooddelivery.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
@lombok.extern.slf4j.Slf4j
@lombok.RequiredArgsConstructor
public class KafkaConfig {
@Bean
    @Primary
    public DefaultErrorHandler defaultErrorHandler(KafkaOperations<Object, Object> kafkaOperations, org.springframework.kafka.core.KafkaAdmin kafkaAdmin) {
        // Standardized Exponential Backoff: Initial interval 1000ms, multiplier 2.0, max 3 retries (max interval 10000ms)
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10000L);
        // Recoverer that sends the failed message to a DLT topic (original topic name + ".DLT")
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaOperations, (consumerRecord, exception) -> {
            String dltTopic = consumerRecord.topic() + ".DLT";
            try {
                kafkaAdmin.createOrModifyTopics(new org.apache.kafka.clients.admin.NewTopic(dltTopic, 1, (short) 1));
            } catch (Exception e) {
                log.warn("Failed to auto-create DLT topic: {}", dltTopic, e);
            }
            log.error("Kafka Message failed after maximum retries. Routing to DLT. Topic: {}, Key: {}, Error: {}", consumerRecord.topic(), consumerRecord.key(), exception.getMessage());
            return new org.apache.kafka.common.TopicPartition(dltTopic, consumerRecord.partition());
        });
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        // Optional: Do not retry for specific fatal exceptions
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class, com.fasterxml.jackson.core.JsonProcessingException.class);
        return errorHandler;
    }
}
