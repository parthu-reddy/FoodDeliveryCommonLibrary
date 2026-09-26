package com.fooddelivery.common.messaging;

import com.fooddelivery.common.util.KafkaHeaderUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.listener.adapter.HandlerAdapter;
import org.springframework.kafka.listener.adapter.RecordMessagingMessageListenerAdapter;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The @DltHandlers log their record's position from Spring-resolved arguments, in the three shapes
 * the platform's handlers use. Calling a handler directly would skip that resolution, so this drives
 * the same listener adapter Spring Kafka invokes a DLT endpoint through.
 */
class DltHandlerPositionBindingTest {

    private static final String EXPECTED = "{\"dltTopic\":\"wallet-events-dlt\",\"partition\":3,\"offset\":41}";

    public static class Handlers {
        String logged;

        /** Shape of RefundCreditConsumer, OrderEventConsumer and six more. */
        public void headersMap(String message, @Headers Map<String, Object> headers) {
            logged = KafkaHeaderUtils.deadLetterPosition(headers);
        }

        /** Shape of CampaignEventConsumer, BillingEventConsumer and seven more. */
        public void namedHeaders(Object message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                 @Header(KafkaHeaders.OFFSET) long offset) {
            logged = KafkaHeaderUtils.deadLetterPosition(topic, partition, offset);
        }

        /** Shape of the two payment/notification handlers: optional payload and exception message, plus the map. */
        public void optionalPayload(@Payload(required = false) String payload,
                                    @Header(name = KafkaHeaders.EXCEPTION_MESSAGE, required = false) String exceptionMessage,
                                    @Headers Map<String, Object> headers) {
            logged = KafkaHeaderUtils.deadLetterPosition(headers);
        }
    }

    private static String deliver(String methodName, Class<?>... parameterTypes) throws Exception {
        Handlers handlers = new Handlers();
        Method method = Handlers.class.getMethod(methodName, parameterTypes);
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.afterPropertiesSet();
        RecordMessagingMessageListenerAdapter<String, String> adapter = new RecordMessagingMessageListenerAdapter<>(handlers, method);
        adapter.setHandlerMethod(new HandlerAdapter(factory.createInvocableHandlerMethod(handlers, method)));

        adapter.onMessage(new ConsumerRecord<>("wallet-events-dlt", 3, 41L, "ORD-1", "{\"refundId\":\"r-1\"}"), null, null);

        return handlers.logged;
    }

    @Test
    void aHandlerTakingTheHeadersMap_logsItsPosition() throws Exception {
        assertThat(deliver("headersMap", String.class, Map.class)).isEqualTo(EXPECTED);
    }

    @Test
    void aHandlerTakingNamedHeaders_logsItsPosition() throws Exception {
        assertThat(deliver("namedHeaders", Object.class, String.class, int.class, long.class)).isEqualTo(EXPECTED);
    }

    @Test
    void aHandlerWithAnOptionalPayload_logsItsPosition() throws Exception {
        assertThat(deliver("optionalPayload", String.class, String.class, Map.class)).isEqualTo(EXPECTED);
    }
}
