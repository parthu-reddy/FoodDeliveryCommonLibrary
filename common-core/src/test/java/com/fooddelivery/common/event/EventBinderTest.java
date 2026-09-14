package com.fooddelivery.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.config.JacksonConfig;
import com.fooddelivery.common.constants.EventType;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class EventBinderTest {

    private EventBinder eventBinder;

    @BeforeEach
    public void setup() {
        // Setup ObjectMapper mimicking JacksonConfig
        JacksonConfig config = new JacksonConfig();
        Jackson2ObjectMapperBuilderCustomizer customizer = config.jsonCustomizer();
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        customizer.customize(builder);
        ObjectMapper objectMapper = builder.build();

        // Setup Validator
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        eventBinder = new EventBinder(objectMapper, validator);
    }

    @Test
    public void testBindIf_MatchesAndBinds() {
        String payload = "{\"orderId\":\"123\",\"gatewayOrderId\":\"456\",\"amount\":1.00,\"gatewayName\":\"RAZORPAY\"}";
        Optional<PaymentRefundRequestedEvent> result = eventBinder.bindIf(
                EventType.PAYMENT_REFUND_REQUESTED, 
                "PAYMENT_REFUND_REQUESTED", 
                payload, 
                PaymentRefundRequestedEvent.class);
        
        assertTrue(result.isPresent());
        assertEquals("123", result.get().getOrderId());
    }

    @Test
    public void testBindIf_NonMatchingType_ReturnsEmpty() {
        String payload = "{\"orderId\":\"123\"}";
        Optional<PaymentRefundRequestedEvent> result = eventBinder.bindIf(
                EventType.PAYMENT_REFUND_REQUESTED, 
                "ORDER_PAID", 
                payload, 
                PaymentRefundRequestedEvent.class);
        
        assertFalse(result.isPresent());
    }

    @Test
    public void testBindIf_UndeclaredBodyEventType_DoesNotBreakBinding() {
        String payload = "{\"orderId\":\"123\",\"eventType\":\"SOME_UNKNOWN_TYPE\"}";
        Optional<PaymentRefundRequestedEvent> result = eventBinder.bindIf(
                EventType.PAYMENT_REFUND_REQUESTED, 
                "PAYMENT_REFUND_REQUESTED", 
                payload, 
                PaymentRefundRequestedEvent.class);
        
        assertTrue(result.isPresent());
        assertEquals("123", result.get().getOrderId());
    }

    @Test
    public void testBindIf_MalformedPayload_Throws() {
        String payload = "{ malformed json }";
        assertThrows(RuntimeException.class, () -> {
            eventBinder.bindIf(
                    EventType.PAYMENT_REFUND_REQUESTED, 
                    "PAYMENT_REFUND_REQUESTED", 
                    payload, 
                    PaymentRefundRequestedEvent.class);
        });
    }

    @Test
    public void testBindIf_MissingNotNullField_ThrowsConstraintViolation() {
        // EventBindingException, not a raw ConstraintViolationException: the type has to be nameable
        // in @RetryableTopic(exclude) so a payload that violates a constraint goes straight to the
        // DLT instead of being retried. The violations are preserved as the cause and in the
        // message -- asserted below, so wrapping cannot quietly lose which field was wrong.
        String payload = "{\"gatewayOrderId\":\"456\"}";
        EventBindingException thrown = assertThrows(EventBindingException.class, () -> {
            eventBinder.bindIf(
                    EventType.PAYMENT_REFUND_REQUESTED, 
                    "PAYMENT_REFUND_REQUESTED", 
                    payload, 
                    PaymentRefundRequestedEvent.class);
        });
        org.junit.jupiter.api.Assertions.assertTrue(thrown.getMessage().contains("orderId"),
                "the DLT record must still say which field was wrong, was: " + thrown.getMessage());
        org.junit.jupiter.api.Assertions.assertInstanceOf(ConstraintViolationException.class,
                thrown.getCause(), "the violations are preserved as the cause");
    }
}
