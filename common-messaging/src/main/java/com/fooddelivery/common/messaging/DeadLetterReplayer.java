package com.fooddelivery.common.messaging;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.InvalidOffsetException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Puts one dead-lettered record back on the topic it failed on, exactly as it was first published.
 *
 * <p>The admin retry endpoints used to take the record's JSON body and republish it. A body is not a
 * record: the key, the {@code eventType}, {@code eventId} and {@code aggregateType} headers, and the
 * exact bytes of the value all live outside it. Every typed listener resolves its event type from the
 * header alone (ADR 002), so each replay was ignored -- a replayed store-credit refund was never
 * credited while the endpoint reported success. The DLT record still carries all of it, so the replay
 * reads that record by its coordinates instead of asking anyone to retype it.
 *
 * <p>Not a Spring bean: {@code com.fooddelivery.common} is component-scanned by services with no
 * Kafka consumer (see OutboxBeansAreNotComponentScannedTest). The admin DLQ controllers build one.
 */
@Slf4j
public class DeadLetterReplayer {

    /** Set by DeadLetterPublishingRecovererFactory -- the {@code @RetryableTopic} path. */
    static final String RETRY_TOPIC_ORIGINAL_TOPIC = "kafka_original-topic";

    /**
     * Added on the way to the DLT, not by the producer: failure diagnostics, the original coordinates,
     * and retry-topic attempt/backoff state. None of it belongs on the replay, and a stale backoff
     * timestamp would be read by the retry machinery.
     */
    private static final List<String> DEAD_LETTER_HEADER_PREFIXES =
            List.of("kafka_dlt-", "kafka_original-", "retry_topic-");

    private final ConsumerFactory<String, String> consumerFactory;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Duration timeout;

    public DeadLetterReplayer(ConsumerFactory<String, String> consumerFactory, KafkaTemplate<String, String> kafkaTemplate) {
        this(consumerFactory, kafkaTemplate, Duration.ofSeconds(10));
    }

    DeadLetterReplayer(ConsumerFactory<String, String> consumerFactory, KafkaTemplate<String, String> kafkaTemplate,
                       Duration timeout) {
        this.consumerFactory = consumerFactory;
        this.kafkaTemplate = kafkaTemplate;
        this.timeout = timeout;
    }

    public DeadLetterReplayResult replay(DeadLetterReplayRequest request) {
        String coordinates = request.dltTopic() + "-" + request.partition() + "@" + request.offset();
        ConsumerRecord<String, String> dead = read(request, coordinates);

        String originalTopic = originalTopic(dead);
        if (originalTopic == null) {
            throw new IllegalArgumentException(coordinates + " is not a dead-letter record: it has neither a "
                    + RETRY_TOPIC_ORIGINAL_TOPIC + " nor a " + KafkaHeaders.DLT_ORIGINAL_TOPIC + " header");
        }

        RecordHeaders headers = new RecordHeaders();
        for (Header header : dead.headers()) {
            if (DEAD_LETTER_HEADER_PREFIXES.stream().noneMatch(header.key()::startsWith)) {
                headers.add(header);
            }
        }
        ProducerRecord<String, String> replay =
                new ProducerRecord<>(originalTopic, null, dead.key(), dead.value(), headers);
        try {
            // Waited on: the endpoint must not report a replay that Kafka never accepted.
            kafkaTemplate.send(replay).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new KafkaException("Interrupted while replaying " + coordinates, e);
        } catch (Exception e) {
            throw new KafkaException("Replay of " + coordinates + " to " + originalTopic + " was not acknowledged", e);
        }

        DeadLetterReplayResult replayed = new DeadLetterReplayResult(originalTopic, dead.key(), text(headers.lastHeader("eventType")),
                text(headers.lastHeader("eventId")));
        log.info("DLT_RECORD_REPLAYED from={} to={} key={} eventType={} eventId={}",
                coordinates, replayed.topic(), replayed.key(), replayed.eventType(), replayed.eventId());
        return replayed;
    }

    private ConsumerRecord<String, String> read(DeadLetterReplayRequest request, String coordinates) {
        Properties overrides = new Properties();
        // Reading a DLT record must not move any group's position on that topic.
        overrides.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        // An offset that has been deleted must fail, not quietly return some other record.
        overrides.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "none");
        overrides.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "1");
        overrides.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        overrides.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        TopicPartition partition = new TopicPartition(request.dltTopic(), request.partition());
        try (Consumer<String, String> consumer = consumerFactory.createConsumer("dead-letter-replay", null, null, overrides)) {
            consumer.assign(List.of(partition));
            consumer.seek(partition, request.offset());
            long deadline = System.nanoTime() + timeout.toNanos();
            while (System.nanoTime() < deadline) {
                for (ConsumerRecord<String, String> found : consumer.poll(Duration.ofMillis(200)).records(partition)) {
                    if (found.offset() != request.offset()) {
                        throw new ResourceNotFoundException("No record at " + coordinates
                                + "; the next record is at offset " + found.offset());
                    }
                    return found;
                }
            }
        } catch (InvalidOffsetException e) {
            throw new ResourceNotFoundException("No record at " + coordinates + ": " + e.getMessage());
        }
        throw new ResourceNotFoundException("No record at " + coordinates + " within " + timeout.toSeconds() + "s");
    }

    /** {@code @RetryableTopic} listeners and the shared DefaultErrorHandler name the original topic differently. */
    private static String originalTopic(ConsumerRecord<String, String> dead) {
        Header retryTopicPath = dead.headers().lastHeader(RETRY_TOPIC_ORIGINAL_TOPIC);
        return retryTopicPath != null ? text(retryTopicPath) : text(dead.headers().lastHeader(KafkaHeaders.DLT_ORIGINAL_TOPIC));
    }

    private static String text(Header header) {
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }
}
