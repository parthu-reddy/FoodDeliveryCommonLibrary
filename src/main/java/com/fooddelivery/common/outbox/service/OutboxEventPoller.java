package com.fooddelivery.common.outbox.service;

import com.fooddelivery.common.constants.AppConstants;
import com.fooddelivery.common.constants.KafkaConstants;
import com.fooddelivery.common.outbox.entity.OutboxEventEntity;
import com.fooddelivery.common.enums.OutboxStatus;
import com.fooddelivery.common.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service("commonOutboxEventPoller")
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPoller {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    @Scheduled(fixedDelayString = "${outbox.poll.interval:5000}")
    public void pollOutboxEvents() {
        List<OutboxEventEntity> unprocessedEvents = transactionTemplate.execute(status -> {
            List<OutboxEventEntity> events = outboxEventRepository.findUnprocessedEventsAndLock(
                    List.of(OutboxStatus.UNPROCESSED, OutboxStatus.FAILED)
            );
            if (!events.isEmpty()) {
                events.forEach(e -> e.setStatus(OutboxStatus.IN_PROGRESS));
                outboxEventRepository.saveAll(events);
            }
            return events;
        });

        if (unprocessedEvents == null || unprocessedEvents.isEmpty()) {
            return;
        }

        log.info("Found {} unprocessed outbox events", unprocessedEvents.size());

        for (OutboxEventEntity event : unprocessedEvents) {
            try {
                String topic = determineTopic(event);

                Message<String> message = MessageBuilder
                        .withPayload(event.getPayload())
                        .setHeader(KafkaHeaders.TOPIC, topic)
                        .setHeader(KafkaHeaders.KEY, event.getAggregateId())
                        .setHeader("eventType", event.getEventType())
                        .build();

                kafkaTemplate.send(message).get(3, TimeUnit.SECONDS);

                event.setStatus(OutboxStatus.PROCESSED);
                event.setProcessedAt(LocalDateTime.now());
                log.info("Successfully published outbox event {} to topic {}", event.getId(), topic);
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}", event.getId(), e);
                int currentRetries = event.getRetryCount() == null ? 0 : event.getRetryCount();
                event.setRetryCount(currentRetries + 1);
                
                if (event.getRetryCount() >= 5) {
                    event.setStatus(OutboxStatus.DLQ);
                    log.error("Outbox event {} moved to DLQ after 5 failed attempts", event.getId());
                } else {
                    event.setStatus(OutboxStatus.FAILED);
                }
                event.setErrorMessage(e.getMessage());
            }
        }
        
        transactionTemplate.executeWithoutResult(status -> {
            outboxEventRepository.saveAll(unprocessedEvents);
        });
    }

    private String determineTopic(OutboxEventEntity event) {
        if (AppConstants.AGGREGATE_PAYMENT.equals(event.getAggregateType())) {
            return KafkaConstants.TOPIC_PAYMENT_EVENTS;
        } else if (AppConstants.AGGREGATE_NOTIFICATION.equals(event.getAggregateType())) {
            return KafkaConstants.TOPIC_NOTIFICATIONS_DISPATCH;
        }
        return KafkaConstants.TOPIC_ORDER_EVENTS;
    }
}
