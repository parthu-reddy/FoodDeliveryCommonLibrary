package com.fooddelivery.common.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import java.util.function.Function;

/**
 * Base class for all Kafka Consumers to enforce idempotency and safe error handling.
 * Consumers must implement this class to prevent poison pill infinite retry loops 
 * and database unique constraint violations.
 */
public abstract class BaseIdempotentConsumer<T> {

    private static final Logger log = LoggerFactory.getLogger(BaseIdempotentConsumer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public BaseIdempotentConsumer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Executes the consumption safely with idempotency checks.
     *
     * @param payload The parsed message payload.
     * @param idempotencyKey The unique identifier for this message to prevent dual processing.
     * @param topic The source topic, used for DLQ routing.
     * @param rawPayload The raw string payload for DLQ routing.
     * @param processor The actual business logic to execute.
     */
    protected void processSafely(T payload, String idempotencyKey, String topic, String rawPayload, Function<T, Boolean> processor) {
        try {
            if (isDuplicate(idempotencyKey)) {
                log.info("Message with idempotency key {} has already been processed. Skipping.", idempotencyKey);
                return;
            }

            boolean success = processor.apply(payload);
            if (success) {
                markAsProcessed(idempotencyKey);
            } else {
                log.warn("Processor indicated failure for message with key {}. Routing to DLQ.", idempotencyKey);
                routeToDlq(rawPayload, topic, new RuntimeException("Processor failed without throwing exception."));
            }

        } catch (Exception ex) {
            log.error("Error processing message with key {}. Routing to DLQ.", idempotencyKey, ex);
            routeToDlq(rawPayload, topic, ex);
        }
    }

    private void routeToDlq(String rawPayload, String originalTopic, Exception ex) {
        String dlqTopic = originalTopic + ".DLT";
        try {
            kafkaTemplate.send(dlqTopic, rawPayload);
            log.info("Successfully routed message to DLQ: {}", dlqTopic);
        } catch (Exception dlqEx) {
            log.error("CRITICAL: Failed to route message to DLQ: {}. Payload might be lost.", dlqTopic, dlqEx);
        }
    }

    /**
     * Checks if the message has already been processed.
     */
    protected abstract boolean isDuplicate(String idempotencyKey);

    /**
     * Marks the message as processed in the idempotent store.
     */
    protected abstract void markAsProcessed(String idempotencyKey);
}
