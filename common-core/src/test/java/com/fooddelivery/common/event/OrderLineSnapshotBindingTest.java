package com.fooddelivery.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.config.JacksonConfig;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** ORDER_PAID carries typed lines: a real body binds, and a line the kitchen could not identify is refused. */
class OrderLineSnapshotBindingTest {

    private EventBinder binder;

    @BeforeEach
    void setUp() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new JacksonConfig().jsonCustomizer().customize(builder);
        ObjectMapper mapper = builder.build();
        binder = new EventBinder(mapper, Validation.buildDefaultValidatorFactory().getValidator());
    }

    private static String event(String items) {
        return """
                {"orderId":"3f2504e0-4f89-41d3-9a0c-0305e82c3301","restaurantId":"9c8b7a65-1e2d-4f30-b5a6-7c8d9e0f1a23",
                 "customerId":"6b1d3c22-9f45-4a7e-8c11-2d4e6f8a9b02","paymentMethod":"CARD","fulfilmentType":"DELIVERY","deliveryLat":12.97,
                 "deliveryLng":77.59,"pickupOtp":"123456","deliveryOtp":"567890","dispatchCityId":"BLR",
                 "fleetSearchRadiusKm":5.0,"totalAmount":250.00,"itemTotal":200.00,"restaurantPlatformFee":20.00,
                 "restaurantDeliveryContribution":0,"platformBonus":0,"restaurantPayout":180.00,"eventType":"ORDER_PAID",
                 "items":%s}""".formatted(items);
    }

    private static final String LINE = """
            {"orderItemId":"11111111-1111-1111-1111-111111111111","menuItemId":"22222222-2222-2222-2222-222222222222",
             "name":"Fried rice","quantity":2,"price":100.00,"categoryId":"33333333-3333-3333-3333-333333333333",
             "categoryName":"Rice","veg":true}""";

    @Test
    void aRealBodyBindsEveryLineField() {
        OrderPaidEvent bound = binder.bind(event("[" + LINE + "]"), OrderPaidEvent.class);
        OrderLineSnapshot line = bound.getItems().get(0);
        assertEquals(UUID.fromString("11111111-1111-1111-1111-111111111111"), line.orderItemId());
        assertEquals(UUID.fromString("22222222-2222-2222-2222-222222222222"), line.menuItemId());
        assertEquals("Fried rice", line.name());
        assertEquals(2, line.quantity());
        assertEquals(0, new BigDecimal("100.00").compareTo(line.price()));
        assertEquals(UUID.fromString("33333333-3333-3333-3333-333333333333"), line.categoryId());
        assertEquals("Rice", line.categoryName());
        assertTrue(line.veg());
    }

    @Test
    void anUncategorisedLineBindsWithNullCategory() {
        String uncategorised = """
                {"orderItemId":"11111111-1111-1111-1111-111111111111","menuItemId":"22222222-2222-2222-2222-222222222222",
                 "name":"Water","quantity":1,"price":20.00}""";
        OrderLineSnapshot line = binder.bind(event("[" + uncategorised + "]"), OrderPaidEvent.class).getItems().get(0);
        assertNull(line.categoryId());
        assertNull(line.categoryName());
        assertNull(line.veg());
    }

    @Test
    void aLineWithoutOrderItemIdIsRefused() {
        String noId = LINE.replace("\"orderItemId\":\"11111111-1111-1111-1111-111111111111\",", "");
        EventBindingException e = assertThrows(EventBindingException.class,
                () -> binder.bind(event("[" + noId + "]"), OrderPaidEvent.class));
        assertTrue(e.getMessage().contains("orderItemId"), e.getMessage());
    }

    @Test
    void emptyItemsAreRefused() {
        EventBindingException e = assertThrows(EventBindingException.class,
                () -> binder.bind(event("[]"), OrderPaidEvent.class));
        assertTrue(e.getMessage().contains("items"), e.getMessage());
    }

    @Test
    void aNonPositiveQuantityIsRefused() {
        assertThrows(EventBindingException.class,
                () -> binder.bind(event("[" + LINE.replace("\"quantity\":2", "\"quantity\":0") + "]"), OrderPaidEvent.class));
    }
}
