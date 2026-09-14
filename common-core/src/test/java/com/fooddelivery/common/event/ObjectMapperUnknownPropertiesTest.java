package com.fooddelivery.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.config.JacksonConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ObjectMapperUnknownPropertiesTest {

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
    public void testUnknownProperty() throws Exception {
        String json = "{\"orderId\":\"123\",\"gatewayOrderId\":\"456\",\"amount\":1.00,\"gatewayName\":\"RAZORPAY\",\"eventType\":\"PAYMENT_REFUND_REQUESTED\"}";
        PaymentRefundRequestedEvent event = objectMapper.readValue(json, PaymentRefundRequestedEvent.class);
        System.out.println("TEST_PASSED_SUCCESSFULLY: " + event.getOrderId());
        assertNotNull(event.getOrderId());
    }
}
