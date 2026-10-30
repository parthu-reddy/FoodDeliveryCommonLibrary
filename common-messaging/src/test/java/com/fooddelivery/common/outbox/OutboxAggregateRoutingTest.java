package com.fooddelivery.common.outbox;

import com.fooddelivery.common.constants.*;
import com.fooddelivery.common.enums.OutboxStatus;
import com.fooddelivery.common.outbox.entity.OutboxEventEntity;
import com.fooddelivery.common.outbox.repository.OutboxEventRepository;
import com.fooddelivery.common.outbox.service.OutboxProcessor;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class OutboxAggregateRoutingTest {
    /** The whole aggregate -> topic table. A consumer listens on the topic named here, so a change is deliberate. */
    static final Map<AggregateType, String> TOPICS = Map.ofEntries(
        Map.entry(AggregateType.ORDER, KafkaConstants.TOPIC_ORDER_EVENTS),
        Map.entry(AggregateType.PAYMENT, KafkaConstants.TOPIC_PAYMENT_EVENTS),
        Map.entry(AggregateType.NOTIFICATION, KafkaConstants.TOPIC_NOTIFICATIONS_DISPATCH),
        Map.entry(AggregateType.OUTLET, KafkaConstants.TOPIC_RESTAURANT_EVENTS),
        Map.entry(AggregateType.BRAND, KafkaConstants.TOPIC_RESTAURANT_EVENTS),
        Map.entry(AggregateType.LEDGER, KafkaConstants.TOPIC_LEDGER_EVENTS),
        Map.entry(AggregateType.ADVERTISEMENT, KafkaConstants.TOPIC_AD_EVENTS),
        Map.entry(AggregateType.WALLET, KafkaConstants.TOPIC_WALLET_EVENTS),
        Map.entry(AggregateType.CHAT_SESSION, KafkaConstants.TOPIC_CHAT_EVENTS),
        Map.entry(AggregateType.REVIEW, KafkaConstants.TOPIC_REVIEW_EVENTS),
        Map.entry(AggregateType.ORGANISATION, KafkaConstants.TOPIC_ORGANISATION_EVENTS),
        Map.entry(AggregateType.DELIVERY_PARTNER, KafkaConstants.TOPIC_DELIVERY_PARTNER_EVENTS),
        Map.entry(AggregateType.MENU, KafkaConstants.TOPIC_MENU_EVENTS),
        Map.entry(AggregateType.KITCHEN_STAFF, KafkaConstants.TOPIC_KITCHEN_EVENTS),
        Map.entry(AggregateType.KITCHEN_ITEM, KafkaConstants.TOPIC_KITCHEN_EVENTS),
        Map.entry(AggregateType.DINING_SESSION, KafkaConstants.TOPIC_DINING_EVENTS));

    @ParameterizedTest @EnumSource(AggregateType.class)
    @SuppressWarnings("unchecked")
    void everyAggregateActuallyPublishes(AggregateType aggregate) {
        var repository = mock(OutboxEventRepository.class);
        KafkaTemplateHolder holder = new KafkaTemplateHolder();
        var event = OutboxEventEntity.builder().id(UUID.randomUUID()).aggregateType(aggregate)
            .aggregateId(UUID.randomUUID().toString()).eventType(EventType.ORGANISATION_CREATED)
            .payload("{}").createdAt(Instant.now()).status(OutboxStatus.UNPROCESSED).build();
        when(repository.findTop100ByStatusInOrderByCreatedAtAsc(any())).thenReturn(List.of(event));
        when(holder.template.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(null));
        new OutboxProcessor(repository, holder.template, new SimpleMeterRegistry()).processOutboxEvents();
        var captor = org.mockito.ArgumentCaptor.forClass(ProducerRecord.class);
        verify(holder.template).send(captor.capture());
        assertThat(TOPICS).containsKey(aggregate);
        assertThat(captor.getValue().topic()).isEqualTo(TOPICS.get(aggregate));
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PROCESSED);
    }
    static class KafkaTemplateHolder {
        @SuppressWarnings("unchecked") final org.springframework.kafka.core.KafkaTemplate<String,String> template = mock(org.springframework.kafka.core.KafkaTemplate.class);
    }
}
