package com.fooddelivery.common.outbox.service;

import com.fooddelivery.common.constants.AggregateType;
import com.fooddelivery.common.constants.KafkaConstants;
import com.fooddelivery.common.enums.OutboxStatus;
import com.fooddelivery.common.outbox.entity.OutboxEventEntity;
import com.fooddelivery.common.outbox.repository.OutboxEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final int MAX_RETRIES = 3;

    /** Read back by {@code KafkaHeaderUtils.extractEventType}. */
    public static final String HEADER_EVENT_TYPE = "eventType";
    public static final String HEADER_AGGREGATE_TYPE = "aggregateType";

    public OutboxProcessor(OutboxEventRepository outboxEventRepository,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEventEntity> events = outboxEventRepository.findTop100ByStatusInOrderByCreatedAtAsc(
                List.of(OutboxStatus.UNPROCESSED, OutboxStatus.FAILED)
        );

        if (events.isEmpty()) {
            return;
        }

        log.debug("Found {} outbox events to process", events.size());

        for (OutboxEventEntity event : events) {
            try {
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

                kafkaTemplate.send(record).get();

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
            default:
                throw new IllegalArgumentException("Unknown aggregate type: " + aggregateType);
        }
    }
}
