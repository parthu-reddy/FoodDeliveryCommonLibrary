package com.fooddelivery.common.messaging;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The headers on these DLT records are the ones Spring Kafka 3.2 actually writes, established by
 * running DeadLetterPublishingRecoverer on a failed record (2026-09-25): the producer's headers survive
 * untouched, and the dead-letter path adds kafka_original-* (via @RetryableTopic) or kafka_dlt-original-*
 * (via the shared DefaultErrorHandler), kafka_dlt-exception-*, and retry_topic-* state.
 */
class DeadLetterReplayerTest {

    private static final String VALUE =
            "{\"eventType\":\"WALLET_CREDIT_REQUESTED\",\"refundId\":\"r-1\",\"orderId\":\"ORD-1\",\"amount\":149.50}";

    private final MockConsumer<String, String> consumer = new MockConsumer<>(OffsetResetStrategy.NONE);
    @SuppressWarnings("unchecked")
    private final ConsumerFactory<String, String> consumerFactory = mock(ConsumerFactory.class);
    private MockProducer<String, String> producer = new MockProducer<>(true, new StringSerializer(), new StringSerializer());

    @BeforeEach
    void setUp() {
        when(consumerFactory.createConsumer(eq("dead-letter-replay"), isNull(), isNull(), any(Properties.class)))
                .thenReturn(consumer);
    }

    private DeadLetterReplayer replayer() {
        return new DeadLetterReplayer(consumerFactory, new KafkaTemplate<>(() -> producer), Duration.ofSeconds(2));
    }

    private static RecordHeaders producerHeaders() {
        RecordHeaders headers = new RecordHeaders();
        headers.add("eventType", bytes("WALLET_CREDIT_REQUESTED"));
        headers.add("eventId", bytes("11111111-2222-3333-4444-555555555555"));
        headers.add("aggregateType", bytes("WALLET"));
        return headers;
    }

    /** What @RetryableTopic's DeadLetterPublishingRecovererFactory leaves on a wallet-events-dlt record. */
    private static RecordHeaders retryTopicDltHeaders() {
        RecordHeaders headers = producerHeaders();
        headers.add("kafka_original-topic", bytes("wallet-events"));
        headers.add("kafka_original-partition", new byte[]{0, 0, 0, 0});
        headers.add("kafka_original-offset", new byte[]{0, 0, 0, 0, 0, 0, 0, 41});
        headers.add("kafka_dlt-exception-fqcn", bytes("org.springframework.kafka.listener.ListenerExecutionFailedException"));
        headers.add("kafka_dlt-exception-message", bytes("wallet credit failed"));
        headers.add("retry_topic-attempts", new byte[]{0, 0, 0, 4});
        headers.add("retry_topic-backoff-timestamp", bytes("1758800000000"));
        return headers;
    }

    private void deadLetterAt(String topic, long offset, RecordHeaders headers) {
        ConsumerRecord<String, String> record = new ConsumerRecord<>(topic, 0, offset, 1758800000000L,
                TimestampType.CREATE_TIME, 0, 0, "ORD-1", VALUE, headers, Optional.empty());
        // Records can only be added once replay() has assigned the partition.
        consumer.schedulePollTask(() -> consumer.addRecord(record));
    }

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private static java.util.List<String> keys(ProducerRecord<String, String> record) {
        return StreamSupport.stream(record.headers().spliterator(), false).map(Header::key).toList();
    }

    @Test
    void retryTopicDeadLetter_isRepublishedToItsOriginalTopic_asTheProducerPublishedIt() {
        deadLetterAt("wallet-events-dlt", 7, retryTopicDltHeaders());

        DeadLetterReplayResult replayed = replayer().replay(new DeadLetterReplayRequest("wallet-events-dlt", 0, 7L));

        assertThat(producer.history()).singleElement().satisfies(sent -> {
            assertThat(sent.topic()).isEqualTo("wallet-events");
            assertThat(sent.key()).isEqualTo("ORD-1");
            // Byte for byte: the old endpoint re-serialised a Map and turned 149.50 into 149.5.
            assertThat(sent.value()).isEqualTo(VALUE);
            assertThat(keys(sent)).containsExactly("eventType", "eventId", "aggregateType");
            assertThat(new String(sent.headers().lastHeader("eventType").value(), StandardCharsets.UTF_8))
                    .isEqualTo("WALLET_CREDIT_REQUESTED");
        });
        assertThat(replayed).isEqualTo(new DeadLetterReplayResult("wallet-events", "ORD-1",
                "WALLET_CREDIT_REQUESTED", "11111111-2222-3333-4444-555555555555"));
    }

    @Test
    void errorHandlerDeadLetter_isRepublishedToItsOriginalTopic() {
        RecordHeaders headers = producerHeaders();
        headers.add(KafkaHeaders.DLT_ORIGINAL_TOPIC, bytes("payment-events"));
        headers.add(KafkaHeaders.DLT_EXCEPTION_MESSAGE, bytes("refund completion failed"));
        deadLetterAt("payment-events.DLT", 3, headers);

        replayer().replay(new DeadLetterReplayRequest("payment-events.DLT", 0, 3L));

        assertThat(producer.history()).singleElement().satisfies(sent -> {
            assertThat(sent.topic()).isEqualTo("payment-events");
            assertThat(keys(sent)).containsExactly("eventType", "eventId", "aggregateType");
        });
    }

    @Test
    void theErrorHandlerHeaderNameIsTheOneSpringKafkaWrites() {
        // Seen on a real DeadLetterPublishingRecoverer output; pinned so a Spring upgrade that renames it is caught.
        assertThat(KafkaHeaders.DLT_ORIGINAL_TOPIC).isEqualTo("kafka_dlt-original-topic");
    }

    @Test
    void aRecordThatIsNotADeadLetter_isRefused_andNothingIsSent() {
        deadLetterAt("wallet-events", 7, producerHeaders());

        assertThatThrownBy(() -> replayer().replay(new DeadLetterReplayRequest("wallet-events", 0, 7L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a dead-letter record");
        assertThat(producer.history()).isEmpty();
    }

    @Test
    void anOffsetWithNoRecord_isNotFound_ratherThanReplayingTheNextOne() {
        deadLetterAt("wallet-events-dlt", 8, retryTopicDltHeaders());

        assertThatThrownBy(() -> replayer().replay(new DeadLetterReplayRequest("wallet-events-dlt", 0, 7L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("next record is at offset 8");
        assertThat(producer.history()).isEmpty();
    }

    @Test
    void readingTheDeadLetter_commitsNothing_failsOnADeletedOffset_andClosesTheConsumer() {
        deadLetterAt("wallet-events-dlt", 7, retryTopicDltHeaders());

        replayer().replay(new DeadLetterReplayRequest("wallet-events-dlt", 0, 7L));

        ArgumentCaptor<Properties> overrides = ArgumentCaptor.forClass(Properties.class);
        verify(consumerFactory).createConsumer(eq("dead-letter-replay"), isNull(), isNull(), overrides.capture());
        assertThat(overrides.getValue())
                .containsEntry(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
                .containsEntry(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "none");
        assertThat(consumer.closed()).isTrue();
    }

    @Test
    void aReplayKafkaNeverAcknowledges_fails() {
        producer = new MockProducer<>(false, new StringSerializer(), new StringSerializer());
        deadLetterAt("wallet-events-dlt", 7, retryTopicDltHeaders());
        DeadLetterReplayer impatient = new DeadLetterReplayer(consumerFactory, new KafkaTemplate<>(() -> producer),
                Duration.ofMillis(300));

        assertThatThrownBy(() -> impatient.replay(new DeadLetterReplayRequest("wallet-events-dlt", 0, 7L)))
                .isInstanceOf(KafkaException.class)
                .hasMessageContaining("was not acknowledged");
    }
}
