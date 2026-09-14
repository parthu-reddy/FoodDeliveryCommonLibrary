package com.fooddelivery.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.config.JacksonConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EventSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        JacksonConfig config = new JacksonConfig();
        Jackson2ObjectMapperBuilderCustomizer customizer = config.jsonCustomizer();
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        customizer.customize(builder);
        objectMapper = builder.build();
    }

    @Test
    public void testSerializationRoundTrip() throws Exception {
        OrderAcceptedEvent e1 = OrderAcceptedEvent.builder().orderId(UUID.randomUUID().toString()).restaurantId("r1").estimatedPrepTimeMinutes(30).build();
        assertEquals(e1, objectMapper.readValue(objectMapper.writeValueAsString(e1), OrderAcceptedEvent.class));

        OrderDelayApprovalRequestedEvent e2 = OrderDelayApprovalRequestedEvent.builder().orderId(UUID.randomUUID().toString()).additionalPrepTimeMinutes(10).build();
        assertEquals(e2, objectMapper.readValue(objectMapper.writeValueAsString(e2), OrderDelayApprovalRequestedEvent.class));

        PaymentRefundRequestedEvent e3 = PaymentRefundRequestedEvent.builder().orderId("123").amount(new BigDecimal("10.50")).build();
        PaymentRefundRequestedEvent deserialized = objectMapper.readValue(objectMapper.writeValueAsString(e3), PaymentRefundRequestedEvent.class);
        assertEquals(e3.getAmount(), deserialized.getAmount());
        assertEquals("123", deserialized.getOrderId());
        
        // Let's test all events
        OrderRejectedEvent e4 = OrderRejectedEvent.builder().orderId("id").reason("reason").build();
        assertEquals(e4, objectMapper.readValue(objectMapper.writeValueAsString(e4), OrderRejectedEvent.class));
        
        DriverAssignedEvent e5 = DriverAssignedEvent.builder().orderId("123").driverId("d1").build();
        assertEquals(e5, objectMapper.readValue(objectMapper.writeValueAsString(e5), DriverAssignedEvent.class));
    }
}
