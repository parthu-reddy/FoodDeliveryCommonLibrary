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
        assertThat(captor.getValue().topic()).isNotBlank();
        if (aggregate == AggregateType.ORGANISATION) {
            assertThat(captor.getValue().topic()).isEqualTo(KafkaConstants.TOPIC_ORGANISATION_EVENTS);
        }
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PROCESSED);
    }
    static class KafkaTemplateHolder {
        @SuppressWarnings("unchecked") final org.springframework.kafka.core.KafkaTemplate<String,String> template = mock(org.springframework.kafka.core.KafkaTemplate.class);
    }
}
