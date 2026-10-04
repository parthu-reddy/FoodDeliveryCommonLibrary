package com.fooddelivery.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.audit.AuditTrail;
import com.fooddelivery.common.constants.*;
import com.fooddelivery.common.enums.*;
import com.fooddelivery.common.event.NotificationRequestEvent;
import com.fooddelivery.common.event.application.*;
import com.fooddelivery.common.outbox.entity.OutboxEventEntity;
import com.fooddelivery.common.outbox.repository.OutboxEventRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Every transition and its audit/outbox/notifications belong to the owning state transaction. */
@RequiredArgsConstructor
public class ApplicationEvents {
    private final OutboxEventRepository outbox;
    private final ObjectMapper mapper;
    private final AuditTrail audit;
    private final NotificationRouterService notifications;
    private final MeterRegistry metrics;

    @Transactional(propagation = Propagation.MANDATORY)
    public void restaurant(UUID id, UUID org, ApplicationStatus status, String reason, long version,
                           Instant now, UUID actor, String actorKind, AuditAction action, List<UUID> recipients) {
        requireTransaction();
        write(AggregateType.BRAND, EventType.RESTAURANT_APPLICATION_STATUS_CHANGED, id,
                "brand-application:" + id + ":" + version,
                new RestaurantApplicationStatusChangedEvent(id, org, status, reason, version, now));
        audit.record(now, actor, actorKind, action, "RESTAURANT_APPLICATION", id, org, reason, Map.of("applicationVersion", version));
        notifyStatus(status, "Restaurant", id, version, recipients);
        committedMetric("restaurant", status);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void delivery(UUID id, ApplicationStatus status, String reason, long version, Instant now,
                         UUID actor, String actorKind, AuditAction action, List<UUID> recipients) {
        requireTransaction();
        write(AggregateType.DELIVERY_PARTNER, EventType.DELIVERY_APPLICATION_STATUS_CHANGED, id,
                "delivery-application:" + id + ":" + version,
                new DeliveryApplicationStatusChangedEvent(id, status, reason, version, now));
        audit.record(now, actor, actorKind, action, "DELIVERY_APPLICATION", id, null, reason, Map.of("applicationVersion", version));
        notifyStatus(status, "Delivery partner", id, version, recipients);
        committedMetric("delivery", status);
    }

    private void write(AggregateType aggregate, EventType type, UUID id, String key, Object event) {
        try {
            outbox.save(OutboxEventEntity.builder().id(UUID.randomUUID()).aggregateType(aggregate)
                    .aggregateId(id.toString()).eventType(type).idempotencyKey(key)
                    .payload(mapper.writeValueAsString(event)).status(OutboxStatus.UNPROCESSED)
                    .createdAt(Instant.now()).retryCount(0).build());
        } catch (JsonProcessingException failure) {
            throw new IllegalStateException("Application event could not be stored");
        }
    }

    private void notifyStatus(ApplicationStatus status, String label, UUID id, long version, List<UUID> recipients) {
        for (UUID recipient : recipients.stream().distinct().toList()) {
            NotificationRequestEvent event;
            switch (status) {
                case APPROVED -> event = NotificationRequestEvent.builder().userId(recipient).channel(ChannelType.PUSH)
                        .eventName(NotificationTemplate.APPLICATION_APPROVED).templateParams(List.of(label)).build();
                case REJECTED -> event = NotificationRequestEvent.builder().userId(recipient).channel(ChannelType.PUSH)
                        .eventName(NotificationTemplate.APPLICATION_REJECTED).templateParams(List.of(label)).build();
                case SUSPENDED -> event = NotificationRequestEvent.builder().userId(recipient).channel(ChannelType.PUSH)
                        .eventName(NotificationTemplate.APPLICATION_SUSPENDED).templateParams(List.of(label)).build();
                default -> { continue; }
            }
            event.setEventId("application-notification:" + id + ":" + version + ":" + recipient);
            event.setPayload(Map.of("applicationId", id.toString(), "applicationType", label, "status", status.name()));
            notifications.routeNotification(event);
        }
    }

    private static void requireTransaction() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Application transition requires a transaction");
        }
    }

    private void committedMetric(String type, ApplicationStatus status) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                metrics.counter("applications.transitions", "type", type, "to", status.name()).increment();
            }
        });
    }
}
