package com.fooddelivery.common.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.util.KafkaHeaderUtils;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.KafkaHeaders;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every dead-letter log line carries KafkaHeaderUtils.deadLetterPosition, and it is only useful if an
 * admin can paste it straight into the retry endpoint. common-core spells Spring's header names out
 * because it has no spring-kafka dependency; this module has one, so the names are pinned here.
 */
class DeadLetterPositionTest {

    @Test
    void aConsumedDeadLetter_isPositionedBySpringKafkasOwnHeaders() {
        // The types Spring Kafka puts in a @Headers map: String topic, Integer partition, Long offset.
        Map<String, Object> headers = Map.of(
                KafkaHeaders.RECEIVED_TOPIC, "wallet-events-dlt",
                KafkaHeaders.RECEIVED_PARTITION, 3,
                KafkaHeaders.OFFSET, 41L);

        assertThat(KafkaHeaderUtils.deadLetterPosition(headers))
                .isEqualTo("{\"dltTopic\":\"wallet-events-dlt\",\"partition\":3,\"offset\":41}");
    }

    @Test
    void theLoggedPosition_isExactlyTheBodyTheRetryEndpointTakes() throws Exception {
        String logged = KafkaHeaderUtils.deadLetterPosition("payment-events.DLT", 0, 7L);

        assertThat(new ObjectMapper().readValue(logged, DeadLetterReplayRequest.class))
                .isEqualTo(new DeadLetterReplayRequest("payment-events.DLT", 0, 7L));
    }
}
