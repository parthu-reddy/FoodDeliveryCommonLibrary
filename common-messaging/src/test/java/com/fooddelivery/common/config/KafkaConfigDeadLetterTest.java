package com.fooddelivery.common.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.messaging.DeadLetterReplayRequest;
import com.fooddelivery.common.messaging.PositionLoggingDeadLetterPublishingRecoverer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.apache.kafka.common.serialization.Serializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ListenerExecutionFailedException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** The shared DefaultErrorHandler's dead-letter path: CustomerApplication's PaymentEventConsumer and three others. */
class KafkaConfigDeadLetterTest {

    private static final Serializer<Object> TEXT = (topic, data) -> data == null ? null : data.toString().getBytes(StandardCharsets.UTF_8);

    private final MockProducer<Object, Object> producer = new MockProducer<>(true, TEXT, TEXT);
    private final KafkaAdmin admin = mock(KafkaAdmin.class);
    private final Logger logger = (Logger) LoggerFactory.getLogger(PositionLoggingDeadLetterPublishingRecoverer.class);
    private final ListAppender<ILoggingEvent> logs = new ListAppender<>();

    @BeforeEach
    void attachLogs() {
        logs.start();
        logger.addAppender(logs);
    }

    @AfterEach
    void detachLogs() {
        logger.detachAppender(logs);
    }

    /** A refund completion that failed on partition 2 of payment-events. */
    private void failOnPartitionTwo() {
        RecordHeaders headers = new RecordHeaders();
        headers.add("eventType", "PAYMENT_REFUNDED".getBytes(StandardCharsets.UTF_8));
        ConsumerRecord<Object, Object> failed = new ConsumerRecord<>("payment-events", 2, 17L, 1758800000000L,
                TimestampType.CREATE_TIME, 0, 0, "ORD-9", "{\"refundId\":\"r-9\"}", headers, Optional.empty());
        KafkaConfig.deadLetterRecoverer(new KafkaTemplate<>(() -> producer), admin)
                .accept(failed, new ListenerExecutionFailedException("listener failed", new IllegalStateException("boom")));
    }

    private List<String> written() {
        return logs.list.stream().filter(e -> e.getLevel() == Level.ERROR)
                .map(ILoggingEvent::getFormattedMessage).filter(m -> m.startsWith("DLT_RECORD_WRITTEN")).toList();
    }

    @Test
    void aRecordFromAnyPartition_reachesTheOnePartitionDlt_withKafkaChoosingThePartition() {
        failOnPartitionTwo();

        assertThat(producer.history()).singleElement().satisfies(dead -> {
            assertThat(dead.topic()).isEqualTo("payment-events.DLT");
            // Not 2: the DLT has one partition, so naming the source partition could never be written.
            assertThat(dead.partition()).isNull();
            assertThat(dead.key()).isEqualTo("ORD-9");
        });
        ArgumentCaptor<NewTopic> created = ArgumentCaptor.forClass(NewTopic.class);
        verify(admin).createOrModifyTopics(created.capture());
        assertThat(created.getValue().numPartitions()).isEqualTo(1);
    }

    @Test
    void whereTheRecordWasWritten_isLoggedAsTheReplayBody() throws Exception {
        failOnPartitionTwo();

        assertThat(written()).singleElement().satisfies(line -> {
            assertThat(line).contains("originalTopic=payment-events", "key=ORD-9", "eventType=PAYMENT_REFUNDED");
            String replay = line.substring(line.indexOf("replay=") + "replay=".length());
            // MockProducer puts the first record on partition 0 at offset 0.
            assertThat(new ObjectMapper().readValue(replay, DeadLetterReplayRequest.class))
                    .isEqualTo(new DeadLetterReplayRequest("payment-events.DLT", 0, 0L));
        });
    }

    @Test
    void aWriteKafkaRejects_isNeverLoggedAsWritten() throws Exception {
        // The send must fail through its future -- the path verifySendResult sees. A send that throws
        // synchronously never reaches it, and would pass whatever this class logged.
        MockProducer<Object, Object> rejecting = new MockProducer<>(false, TEXT, TEXT);
        RecordHeaders headers = new RecordHeaders();
        ConsumerRecord<Object, Object> failed = new ConsumerRecord<>("payment-events", 2, 17L, 1758800000000L,
                TimestampType.CREATE_TIME, 0, 0, "ORD-9", "{}", headers, Optional.empty());
        var recoverer = KafkaConfig.deadLetterRecoverer(new KafkaTemplate<>(() -> rejecting), admin);
        recoverer.setWaitForSendResultTimeout(java.time.Duration.ofSeconds(5));
        Thread broker = new Thread(() -> {
            while (rejecting.history().isEmpty()) Thread.onSpinWait();
            rejecting.errorNext(new org.apache.kafka.common.KafkaException("broker down"));
        });
        broker.start();

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                recoverer.accept(failed, new ListenerExecutionFailedException("listener failed", new IllegalStateException("boom"))));
        broker.join(5000);

        assertThat(written()).isEmpty();
    }
}
