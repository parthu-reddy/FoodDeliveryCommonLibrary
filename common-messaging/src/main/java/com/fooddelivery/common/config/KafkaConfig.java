package com.fooddelivery.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import com.fooddelivery.common.messaging.PositionLoggingDeadLetterPublishingRecoverer;

@Configuration
@lombok.extern.slf4j.Slf4j
@lombok.RequiredArgsConstructor
public class KafkaConfig {
    @Bean
    public DefaultErrorHandler defaultErrorHandler(org.springframework.kafka.core.KafkaTemplate<Object, Object> kafkaTemplate, org.springframework.kafka.core.KafkaAdmin kafkaAdmin) {
        // Standardized Exponential Backoff: Initial interval 1000ms, multiplier 2.0, max 3 retries (max interval 10000ms)
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10000L);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(deadLetterRecoverer(kafkaTemplate, kafkaAdmin), backOff);
        // Optional: Do not retry for specific fatal exceptions
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class, com.fasterxml.jackson.core.JsonProcessingException.class);
        return errorHandler;
    }

    /**
     * Sends the failed message to {@code <topic>.DLT} and logs where it was written, as the body the admin
     * DLQ retry endpoints replay by.
     *
     * <p>Partition -1 lets Kafka choose from the record's key. The DLT is created with one partition, and
     * routing to the failed record's own partition -- as this did -- fails for any record not read from
     * partition 0 the moment a source topic has more than one.
     */
    static DeadLetterPublishingRecoverer deadLetterRecoverer(KafkaOperations<?, ?> kafkaTemplate,
                                                            org.springframework.kafka.core.KafkaAdmin kafkaAdmin) {
        return new PositionLoggingDeadLetterPublishingRecoverer(kafkaTemplate, (consumerRecord, exception) -> {
            String dltTopic = consumerRecord.topic() + ".DLT";
            try {
                kafkaAdmin.createOrModifyTopics(new org.apache.kafka.clients.admin.NewTopic(dltTopic, 1, (short) 1));
            } catch (Exception e) {
                log.warn("Failed to auto-create DLT topic: {}", dltTopic, e);
            }
            log.error("Kafka Message failed after maximum retries. Routing to DLT. Topic: {}, Key: {}, Error: {}", consumerRecord.topic(), consumerRecord.key(), exception.getMessage());
            return new org.apache.kafka.common.TopicPartition(dltTopic, -1);
        });
    }
}
