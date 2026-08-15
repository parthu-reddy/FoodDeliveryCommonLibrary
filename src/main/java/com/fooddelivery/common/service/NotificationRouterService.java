package com.fooddelivery.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.constants.KafkaConstants;
import com.fooddelivery.common.event.NotificationRequestEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@lombok.extern.slf4j.Slf4j
public class NotificationRouterService {
    @java.lang.SuppressWarnings("all")

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void routeNotification(NotificationRequestEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            String routingKey = event.getUserId() != null ? event.getUserId().toString() : event.getExplicitRecipient();
            log.info("Triggering event: NOTIFICATION_DISPATCH for aggregate: {}", routingKey);
            kafkaTemplate.send(KafkaConstants.TOPIC_NOTIFICATIONS_DISPATCH, routingKey, payload).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to asynchronously publish NotificationRequestEvent for aggregate: {}", routingKey, ex);
                } else if (result != null && result.getRecordMetadata() != null) {
                    log.info("Successfully published NotificationRequestEvent to {} with offset: {}", KafkaConstants.TOPIC_NOTIFICATIONS_DISPATCH, result.getRecordMetadata().offset());
                } else {
                    log.info("Successfully published NotificationRequestEvent to {}", KafkaConstants.TOPIC_NOTIFICATIONS_DISPATCH);
                }
            });
        } catch (Exception e) {
            log.error("Failed to serialize NotificationRequestEvent", e);
            throw new RuntimeException("Failed to route notification", e);
        }
    }

    @java.lang.SuppressWarnings("all")
    public NotificationRouterService(final KafkaTemplate<String, String> kafkaTemplate, final ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
}
