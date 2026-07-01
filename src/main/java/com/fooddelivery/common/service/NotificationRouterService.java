package com.fooddelivery.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.constants.KafkaConstants;
import com.fooddelivery.common.event.NotificationRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRouterService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void routeNotification(NotificationRequestEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            String routingKey = event.getUserId() != null ? event.getUserId().toString() : event.getExplicitRecipient();
            
            kafkaTemplate.send(KafkaConstants.TOPIC_NOTIFICATIONS_DISPATCH, routingKey, payload)
                .get(3, java.util.concurrent.TimeUnit.SECONDS);
            log.info("Successfully published NotificationRequestEvent to {}", KafkaConstants.TOPIC_NOTIFICATIONS_DISPATCH);
        } catch (Exception e) {
            log.error("Failed to serialize or publish NotificationRequestEvent", e);
            throw new RuntimeException("Failed to route notification", e);
        }
    }
}
