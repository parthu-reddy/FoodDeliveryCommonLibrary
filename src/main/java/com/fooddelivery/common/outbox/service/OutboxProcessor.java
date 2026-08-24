package com.fooddelivery.common.outbox.service;

import com.fooddelivery.common.constants.AggregateType;
import com.fooddelivery.common.constants.KafkaConstants;
import com.fooddelivery.common.enums.OutboxStatus;
import com.fooddelivery.common.outbox.entity.OutboxEventEntity;
import com.fooddelivery.common.outbox.repository.OutboxEventRepository;
import com.fooddelivery.common.util.EventPayloadUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.instrument.MeterRegistry;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MeterRegistry meterRegistry;
    private static final int MAX_RETRIES = 3;

    /** Read back by {@code KafkaHeaderUtils.extractEventType}. */
    public static final String HEADER_EVENT_TYPE = "eventType";
    public static final String HEADER_AGGREGATE_TYPE = "aggregateType";

    public OutboxProcessor(OutboxEventRepository outboxEventRepository,
                           KafkaTemplate<String, String> kafkaTemplate,
                           MeterRegistry meterRegistry) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.meterRegistry = meterRegistry;
        this.backlogAgeSeconds = meterRegistry.gauge("outbox_backlog_age_seconds", new java.util.concurrent.atomic.AtomicLong(0));
    }

    private final java.util.concurrent.atomic.AtomicLong backlogAgeSeconds;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEventEntity> events = outboxEventRepository.findTop100ByStatusInOrderByCreatedAtAsc(
                List.of(OutboxStatus.UNPROCESSED, OutboxStatus.FAILED)
        );

        if (events.isEmpty()) {
            this.backlogAgeSeconds.set(0);
            return;
        } else {
            long ageInSeconds = java.time.Duration.between(events.get(0).getCreatedAt(), LocalDateTime.now()).getSeconds();
            this.backlogAgeSeconds.set(ageInSeconds);
        }

        log.debug("Found {} outbox events to process", events.size());

        for (OutboxEventEntity event : events) {
            try {
                // The event type must appear once, or identically in both places. A row saying
                // REFUND_GENERATED (a credit) under a payload saying REVERSAL_GENERATED (a debit)
                // publishes fine and moves money whichever way the consumer's resolver prefers.
                // Reject it here -- every producer on the platform passes through this loop, so
                // this is the one place the rule can hold without an exception list. See ADR 002.
                if (event.getEventType() != null) {
                    String conflicting = EventPayloadUtils.conflictingBodyEventType(
                            event.getPayload(), event.getEventType().name());
                    if (conflicting != null) {
                        throw new IllegalStateException(String.format(
                                "Outbox event %s contradicts itself: row eventType=%s but payload "
                                        + "eventType=%s. Publish one or make them agree (ADR 002).",
                                event.getId(), event.getEventType().name(), conflicting));
                    }
                }

                String topic = getTopicForAggregateType(event.getAggregateType());

                // Using aggregateId as the Kafka partition key to ensure ordered processing per aggregate
                ProducerRecord<String, String> record =
                        new ProducerRecord<>(topic, event.getAggregateId(), event.getPayload());

                // The outbox row has always carried the event type, but it was never transmitted:
                // consumers were left inferring it from the JSON body, and @Header("eventType")
                // listeners could not be satisfied at all. Publish it as a real Kafka header.
                if (event.getEventType() != null) {
                    record.headers().add(HEADER_EVENT_TYPE,
                            event.getEventType().name().getBytes(StandardCharsets.UTF_8));
                }
                if (event.getAggregateType() != null) {
                    record.headers().add(HEADER_AGGREGATE_TYPE,
                            event.getAggregateType().name().getBytes(StandardCharsets.UTF_8));
                }
                if (event.getId() != null) {
                    record.headers().add("eventId",
                            event.getId().toString().getBytes(StandardCharsets.UTF_8));
                }

                kafkaTemplate.send(record).get(5, java.util.concurrent.TimeUnit.SECONDS);

                event.setStatus(OutboxStatus.PROCESSED);
                event.setProcessedAt(LocalDateTime.now());
                event.setErrorMessage(null);
                log.debug("Successfully processed outbox event id: {}", event.getId());
            } catch (Exception e) {
                log.error("Failed to process outbox event id: {}", event.getId(), e);
                int retries = event.getRetryCount() == null ? 0 : event.getRetryCount();
                event.setRetryCount(retries + 1);
                event.setErrorMessage(e.getMessage());
                
                if (event.getRetryCount() >= MAX_RETRIES) {
                    event.setStatus(OutboxStatus.DLQ);
                    log.warn("Outbox event id: {} moved to DLQ after {} retries", event.getId(), event.getRetryCount());
                } else {
                    event.setStatus(OutboxStatus.FAILED);
                }
            }
        }
        
        outboxEventRepository.saveAll(events);
    }

    private String getTopicForAggregateType(AggregateType aggregateType) {
        switch (aggregateType) {
            case ORDER:
                return KafkaConstants.TOPIC_ORDER_EVENTS;
            case PAYMENT:
                return KafkaConstants.TOPIC_PAYMENT_EVENTS;
            case NOTIFICATION:
                return KafkaConstants.TOPIC_NOTIFICATIONS_DISPATCH;
            case OUTLET:
            case BRAND:
                return KafkaConstants.TOPIC_RESTAURANT_EVENTS;
            case LEDGER:
                return KafkaConstants.TOPIC_LEDGER_EVENTS;
            case ADVERTISEMENT:
                return KafkaConstants.TOPIC_AD_EVENTS;
            case WALLET:
                return KafkaConstants.TOPIC_WALLET_EVENTS;
            case CHAT_SESSION:
                return KafkaConstants.TOPIC_CHAT_EVENTS;
            case REVIEW:
                return KafkaConstants.TOPIC_REVIEW_EVENTS;
            default:
                throw new IllegalArgumentException("Unknown aggregate type: " + aggregateType);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupOutboxEvents() {
        LocalDateTime retentionThreshold = LocalDateTime.now().minusDays(7);
        int deleted = outboxEventRepository.deleteProcessedEventsOlderThan(
            OutboxStatus.PROCESSED, retentionThreshold
        );
        log.info("Cleaned up {} processed outbox events older than {}", deleted, retentionThreshold);
    }
}
