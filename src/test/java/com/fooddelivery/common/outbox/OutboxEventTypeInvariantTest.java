package com.fooddelivery.common.outbox;

import com.fooddelivery.common.constants.AggregateType;
import com.fooddelivery.common.constants.EventType;
import com.fooddelivery.common.enums.OutboxStatus;
import com.fooddelivery.common.outbox.entity.OutboxEventEntity;
import com.fooddelivery.common.outbox.repository.OutboxEventRepository;
import com.fooddelivery.common.outbox.service.OutboxProcessor;
import com.fooddelivery.common.util.EventPayloadUtils;
import com.fooddelivery.common.util.KafkaHeaderUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * An event's type may live on the outbox row alone, or on the row and in the payload saying the
 * same thing. It may never say two different things.
 *
 * <p>Why it matters: the platform has two resolvers with opposite precedence --
 * {@link KafkaHeaderUtils#extractEventType} reads the Kafka header first,
 * {@link EventPayloadUtils#resolveEventType} reads the body first. On {@code wallet-events} a
 * contradictory event therefore means the direction of a wallet movement -- credit the customer, or
 * debit the restaurant -- is decided by which helper the consumer happens to call. That exact
 * contradiction shipped once (ADR 002).
 *
 * <p>These tests drive {@link OutboxProcessor} itself rather than a hand-written copy of a payload,
 * because the previous guard asserted against a JSON literal with no link to any producer: the
 * producer could drift away from it and the test would stay green. It did, and it did.
 */
class OutboxEventTypeInvariantTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private OutboxEventRepository repository;
    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
    private OutboxProcessor processor;

    @BeforeEach
    void setUp() {
        repository = mock(OutboxEventRepository.class);
        processor = new OutboxProcessor(repository, kafkaTemplate, new SimpleMeterRegistry());
    }

    private OutboxEventEntity walletEvent(EventType rowType, String payload) {
        return OutboxEventEntity.builder()
                .id(UUID.randomUUID())
                .aggregateType(AggregateType.WALLET)
                .aggregateId(UUID.randomUUID().toString())
                .eventType(rowType)
                .payload(payload)
                .createdAt(LocalDateTime.now())
                .status(OutboxStatus.UNPROCESSED)
                .build();
    }

    private void given(OutboxEventEntity event) {
        when(repository.findTop100ByStatusInOrderByCreatedAtAsc(any())).thenReturn(List.of(event));
    }

    /** The real reversal payload: AdminOrderManualController repeats the type, agreeing with the row. */
    private static String reversalPayload(String bodyEventType) {
        return "{\"entityId\":\"9c8b7a65-1e2d-4f30-b5a6-7c8d9e0f1a23\",\"entityType\":\"RESTAURANT\","
                + "\"amount\":\"40.00\",\"referenceId\":\"REV_3f2504e0_1699999999999\","
                + "\"description\":\"Reversal for order 3f2504e0\",\"chargeCategory\":\"REFUND\","
                + "\"eventType\":\"" + bodyEventType + "\"}";
    }

    @Test
    void aContradictoryEventIsNeverPublished() {
        // The original bug: row says credit the customer, body says debit the restaurant.
        OutboxEventEntity event = walletEvent(EventType.PAYMENT_REFUNDED,
                reversalPayload("PAYMENT_PARTIALLY_REFUNDED"));
        given(event);

        processor.processOutboxEvents();

        verify(kafkaTemplate, never()).send(any(ProducerRecord.class));
        assertThat(event.getStatus())
                .describedAs("a self-contradicting event must not reach consumers")
                .isNotEqualTo(OutboxStatus.PROCESSED);
        assertThat(event.getErrorMessage()).contains("contradicts itself");
    }

    @Test
    @SuppressWarnings("unchecked")
    void anAgreeingEventPublishesWithTheTypeOnTheHeader() throws Exception {
        OutboxEventEntity event = walletEvent(EventType.PAYMENT_PARTIALLY_REFUNDED,
                reversalPayload("PAYMENT_PARTIALLY_REFUNDED"));
        given(event);
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        processor.processOutboxEvents();

        ArgumentCaptor<ProducerRecord<String, String>> sent =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(sent.capture());
        assertThat(new String(sent.getValue().headers().lastHeader("eventType").value(), StandardCharsets.UTF_8)).isEqualTo("PAYMENT_PARTIALLY_REFUNDED");
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PROCESSED);
    }

    @Test
    @SuppressWarnings("unchecked")
    void aPayloadThatOmitsTheTypeIsFine() {
        // Not every producer repeats the type; the row alone is a complete answer.
        OutboxEventEntity event = walletEvent(EventType.PAYMENT_PARTIALLY_REFUNDED,
                "{\"entityId\":\"9c8b7a65-1e2d-4f30-b5a6-7c8d9e0f1a23\",\"amount\":\"40.00\"}");
        given(event);
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        processor.processOutboxEvents();

        verify(kafkaTemplate).send(any(ProducerRecord.class));
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PROCESSED);
    }

    @Test
    void bothResolversAgreeOnAnyEventTheInvariantAdmits() throws Exception {
        JsonNode body = MAPPER.readTree(reversalPayload("PAYMENT_PARTIALLY_REFUNDED"));
        Map<String, Object> headers = new HashMap<>();
        headers.put("eventType", "PAYMENT_PARTIALLY_REFUNDED".getBytes(StandardCharsets.UTF_8));

        assertThat(KafkaHeaderUtils.extractEventType(headers, body)).isEqualTo("PAYMENT_PARTIALLY_REFUNDED");
        assertThat(EventPayloadUtils.resolveEventType(body, headers))
                .describedAs("opposite precedence, same answer -- which is what the invariant buys")
                .isEqualTo("PAYMENT_PARTIALLY_REFUNDED");
    }

    @Test
    void theCheckIgnoresPayloadsWithNothingToContradict() {
        assertThat(EventPayloadUtils.conflictingBodyEventType(null, "X")).isNull();
        assertThat(EventPayloadUtils.conflictingBodyEventType("{}", null)).isNull();
        assertThat(EventPayloadUtils.conflictingBodyEventType("not json", "X")).isNull();
        assertThat(EventPayloadUtils.conflictingBodyEventType("[1,2]", "X")).isNull();
        assertThat(EventPayloadUtils.conflictingBodyEventType("{\"a\":1}", "X")).isNull();
        assertThat(EventPayloadUtils.conflictingBodyEventType("{\"eventType\":\"X\"}", "X")).isNull();
        assertThat(EventPayloadUtils.conflictingBodyEventType("{\"eventType\":\"Y\"}", "X"))
                .isEqualTo("Y");
    }
}
