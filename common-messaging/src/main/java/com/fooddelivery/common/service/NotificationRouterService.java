package com.fooddelivery.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.constants.KafkaConstants;
import com.fooddelivery.common.event.NotificationRequestEvent;
import com.fooddelivery.common.outbox.repository.OutboxEventRepository;
import com.fooddelivery.common.outbox.entity.OutboxEventEntity;
import com.fooddelivery.common.constants.AggregateType;

/**
 * Routes notification requests through the transactional outbox.
 *
 * <p>Supplied by {@code OutboxConfiguration}, not component-scanned: it needs an
 * {@link OutboxEventRepository}, and forcing that on every service that scans
 * {@code com.fooddelivery.common} stopped three of them from starting.
 */
@lombok.extern.slf4j.Slf4j
@lombok.RequiredArgsConstructor
public class NotificationRouterService {
private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void routeNotification(NotificationRequestEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            String routingKey = event.getUserId() != null ? event.getUserId().toString() : event.getExplicitRecipient();
            OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                .id(java.util.UUID.randomUUID())
                .aggregateId((event.getUserId() != null ? event.getUserId() : java.util.UUID.randomUUID()).toString())
                .aggregateType(AggregateType.NOTIFICATION)
                .eventType(com.fooddelivery.common.constants.EventType.NOTIFICATION_DISPATCH)
                .payload(payload)
                .createdAt(java.time.Instant.now())
                .build();
            outboxEventRepository.save(outboxEvent);
            log.info("Successfully persisted NotificationRequestEvent outbox event for aggregate: {}", routingKey);
        } catch (Exception e) {
            log.error("Failed to serialize NotificationRequestEvent", e);
            throw new RuntimeException("Failed to route notification", e);
        }
    }

}
