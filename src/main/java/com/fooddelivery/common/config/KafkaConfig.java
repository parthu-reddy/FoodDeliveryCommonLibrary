package com.fooddelivery.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@Slf4j
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler defaultErrorHandler(KafkaOperations<Object, Object> kafkaOperations) {
        // Retry up to 3 times with a 2-second delay between attempts
        FixedBackOff fixedBackOff = new FixedBackOff(2000L, 3);
        
        // Recoverer that sends the failed message to a DLQ topic (original topic name + ".DLQ")
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaOperations,
                (consumerRecord, exception) -> {
                    log.error("Kafka Message failed after maximum retries. Routing to DLQ. Topic: {}, Key: {}, Error: {}",
                            consumerRecord.topic(), consumerRecord.key(), exception.getMessage());
                    return new org.apache.kafka.common.TopicPartition(consumerRecord.topic() + ".DLQ", consumerRecord.partition());
                });

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, fixedBackOff);

        // Optional: Do not retry for specific fatal exceptions
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        
        return errorHandler;
    }
}
