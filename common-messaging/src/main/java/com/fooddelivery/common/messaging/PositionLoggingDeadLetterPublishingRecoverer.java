package com.fooddelivery.common.messaging;

import com.fooddelivery.common.util.KafkaHeaderUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.SendResult;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * A DeadLetterPublishingRecoverer that logs where each dead-letter record was written, as the body the
 * admin DLQ retry endpoints take.
 *
 * <p>For the shared DefaultErrorHandler's {@code <topic>.DLT} topics. Nothing consumes them, so unlike
 * a {@code @RetryableTopic} listener's {@code @DltHandler} there is no other place a record's position
 * is ever seen; without this line it could only be found with Kafka tooling.
 */
@Slf4j
public class PositionLoggingDeadLetterPublishingRecoverer extends DeadLetterPublishingRecoverer {

    public PositionLoggingDeadLetterPublishingRecoverer(KafkaOperations<?, ?> template,
            BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> destinationResolver) {
        super(template, destinationResolver);
    }

    @Override
    protected void verifySendResult(KafkaOperations<Object, Object> kafkaTemplate, ProducerRecord<Object, Object> outRecord,
            CompletableFuture<SendResult<Object, Object>> sendResult, ConsumerRecord<?, ?> inRecord) {
        sendResult.whenComplete((result, failure) -> {
            if (failure == null) {
                RecordMetadata written = result.getRecordMetadata();
                Header eventType = inRecord.headers().lastHeader("eventType");
                log.error("DLT_RECORD_WRITTEN originalTopic={} key={} eventType={} replay={}",
                        inRecord.topic(), inRecord.key(),
                        eventType == null ? null : new String(eventType.value(), StandardCharsets.UTF_8),
                        KafkaHeaderUtils.deadLetterPosition(written.topic(), written.partition(), written.offset()));
            }
        });
        // Unchanged: waits for the send and fails the recovery if it did not succeed.
        super.verifySendResult(kafkaTemplate, outRecord, sendResult, inRecord);
    }
}
