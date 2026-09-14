package com.fooddelivery.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.config.JacksonConfig;
import com.fooddelivery.common.constants.NotificationTemplate;
import com.fooddelivery.common.enums.ChannelType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EventBinder must bind the ROOT of the message, never {@code root.get("payload")}.
 *
 * <p>The first cut called {@code EventPayloadUtils.unwrapPayload} inside {@code bind}. That helper
 * returns the {@code payload} child whenever the key is present, and
 * {@link NotificationRequestEvent} has a {@code payload} field of its own. IdentityService
 * publishes OTP logins with {@code payload(Map.of("otp", otp))}, so every OTP notification unwrapped
 * to {@code {"otp":"..."}} and bound to an event with eventName, channel and recipient all null --
 * no exception, no DLT entry, no SMS. Measured 2026-09-13:
 * {@code BOUND eventName=null channel=null recipient=null templateParams=null}.
 *
 * <p>No producer on any Kafka topic in this platform emits an {@code {eventType, payload}} envelope;
 * the outbox publishes the business object flat. The unwrap bought nothing and cost this.
 */
public class EventBinderPayloadCollisionTest {

    private ObjectMapper mapper() {
        Jackson2ObjectMapperBuilder b = new Jackson2ObjectMapperBuilder();
        Jackson2ObjectMapperBuilderCustomizer c = new JacksonConfig().jsonCustomizer();
        c.customize(b);
        return b.build();
    }

    private EventBinder binder() {
        Validator v = Validation.buildDefaultValidatorFactory().getValidator();
        return new EventBinder(mapper(), v);
    }

    @Test
    public void bindsAnEventClassThatHasItsOwnPayloadField() throws Exception {
        // Exactly what IdentityService.OutboxEventPublisherAdapter writes to the outbox.
        NotificationRequestEvent sent = NotificationRequestEvent.builder()
                .explicitRecipient("+919999999999")
                .channel(ChannelType.SMS)
                .eventName(NotificationTemplate.OTP_LOGIN)
                .templateParams(List.of("123456"))
                .payload(Map.of("otp", "123456"))
                .build();

        NotificationRequestEvent got =
                binder().bind(mapper().writeValueAsString(sent), NotificationRequestEvent.class);

        assertEquals(NotificationTemplate.OTP_LOGIN, got.getEventName(), "eventName survives binding");
        assertEquals(ChannelType.SMS, got.getChannel(), "channel survives binding");
        assertEquals("+919999999999", got.getExplicitRecipient(), "recipient survives binding");
        assertEquals(List.of("123456"), got.getTemplateParams(), "templateParams survive binding");
        assertEquals(Map.of("otp", "123456"), got.getPayload(), "the payload field is itself preserved");
    }

    @Test
    public void orderUuidIsNotSerialisedOntoTheWire() throws Exception {
        // OrderScopedEvent.orderUuid() is deliberately not named getOrderUuid: Jackson's bean
        // introspection would then publish an `orderUuid` key that no producer sends and no
        // contract pins, silently widening every order event's wire shape.
        OrderCancelledEvent e = OrderCancelledEvent.builder()
                .orderId(UUID.randomUUID().toString()).reason("test").build();
        String wire = mapper().writeValueAsString(e);
        assertFalse(wire.contains("orderUuid"), "orderUuid must not appear on the wire: " + wire);
        assertNotNull(e.orderUuid(), "but it must still resolve in code");
    }

    @Test
    public void orderUuidHandlesAMissingOrBlankOrderId() {
        assertEquals(null, OrderCancelledEvent.builder().orderId(null).build().orderUuid(),
                "a null orderId yields null, not an exception the DLT would swallow as a parse error");
        assertEquals(null, OrderCancelledEvent.builder().orderId("").build().orderUuid(),
                "a blank orderId yields null rather than IllegalArgumentException from UUID.fromString");
    }

    @Test
    public void bindsUnderAStrictObjectMapperToo() throws Exception {
        // The production mapper disables FAIL_ON_UNKNOWN_PROPERTIES; a plain `new ObjectMapper()`
        // does not, and several test harnesses build one. This asserts the event classes carry
        // their own @JsonIgnoreProperties so binding does not depend on which mapper is wired --
        // PaymentEventConsumerRefundIdempotencyTest threw UnrecognizedPropertyException on the
        // body `eventType` until they did.
        ObjectMapper strict = new ObjectMapper();
        EventBinder strictBinder = new EventBinder(
                strict, Validation.buildDefaultValidatorFactory().getValidator());
        String wire = "{\"orderId\":\"123\",\"gatewayOrderId\":\"456\",\"amount\":1.00,"
                + "\"gatewayName\":\"RAZORPAY\",\"eventType\":\"PAYMENT_REFUND_REQUESTED\"}";
        PaymentRefundRequestedEvent got =
                strictBinder.bind(wire, PaymentRefundRequestedEvent.class);
        assertEquals("123", got.getOrderId());
    }

    @Test
    public void aNullPayloadFailsWithAMessageAboutTheEventNotHibernate() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> binder().bind("null", PaymentRefundRequestedEvent.class));
        assertTrue(e.getMessage().contains("PaymentRefundRequestedEvent"),
                "the message must name the event type, was: " + e.getMessage());
    }
}
