package com.fooddelivery.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fooddelivery.common.config.JacksonConfig;
import com.fooddelivery.common.enums.PaymentMethod;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The six order-milestone events carry the producer's {@code occurredAt}, and a body without it does not bind.
 *
 * <p>A milestone with no occurrence time would leave the consumer to use its own clock, which is the receive time: Kafka
 * lag, a retry back-off or a DLT replay would then move the milestone (RandomDocuments/DelayPenalties_2026-10-25,
 * Phase 1). Each event is serialized by Boot's mapper, as the outbox writes it, so the wire form is ISO-8601.
 */
class OccurredAtBindingTest {

    private static final String ORDER_ID = "3f2504e0-4f89-41d3-9a0c-0305e82c3301";
    private static final Instant AT = Instant.parse("2026-10-25T09:30:00Z");

    private EventBinder binder;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        AtomicReference<ObjectMapper> boot = new AtomicReference<>();
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
                .withUserConfiguration(JacksonConfig.class).run(ctx -> boot.set(ctx.getBean(ObjectMapper.class)));
        mapper = boot.get();
        binder = new EventBinder(mapper, Validation.buildDefaultValidatorFactory().getValidator());
    }

    private OrderAcceptedEvent accepted() {
        return OrderAcceptedEvent.builder().orderId(ORDER_ID).restaurantId("9c8b7a65-1e2d-4f30-b5a6-7c8d9e0f1a23")
                .restaurantLat(12.97).restaurantLng(77.59).deliveryLat(12.93).deliveryLng(77.62)
                .pickupOtp("123456").deliveryOtp("567890").dispatchCityId("BLR").fleetSearchRadiusKm(5.0)
                .paymentMethod(PaymentMethod.CARD).estimatedCompletionTime(AT.toEpochMilli() + 20 * 60_000L)
                .occurredAt(AT).build();
    }

    private OrderReadyEvent ready() {
        return OrderReadyEvent.builder().orderId(ORDER_ID).restaurantId("r").occurredAt(AT).build();
    }

    private DriverAssignedEvent assigned() {
        return DriverAssignedEvent.builder().orderId(ORDER_ID).driverId("d1").occurredAt(AT).build();
    }

    private DriverAtRestaurantEvent atRestaurant() {
        return DriverAtRestaurantEvent.builder().orderId(ORDER_ID).driverId("d1").occurredAt(AT).build();
    }

    private OrderStatusUpdatedEvent statusUpdated() {
        return OrderStatusUpdatedEvent.builder().orderId(ORDER_ID).status("OUT_FOR_DELIVERY").occurredAt(AT).build();
    }

    private DeliveredEvent delivered() {
        return DeliveredEvent.builder().orderId(ORDER_ID).status("DELIVERED").occurredAt(AT).build();
    }

    /** The event as JSON with its occurredAt key removed. */
    private String withoutOccurredAt(Object event) throws Exception {
        ObjectNode node = mapper.valueToTree(event);
        assertNotNull(node.remove("occurredAt"), "the sample must carry occurredAt before it is removed");
        return mapper.writeValueAsString(node);
    }

    private <T> void roundTrips(T event, Class<T> type) throws Exception {
        String json = mapper.writeValueAsString(event);
        assertTrue(json.contains("\"occurredAt\":\"2026-10-25T09:30:00Z\""), json);
        assertEquals(event, binder.bind(json, type));
    }

    @Test
    void everyStampedEventRoundTripsItsOccurredAtAsIso8601() throws Exception {
        roundTrips(accepted(), OrderAcceptedEvent.class);
        roundTrips(ready(), OrderReadyEvent.class);
        roundTrips(assigned(), DriverAssignedEvent.class);
        roundTrips(atRestaurant(), DriverAtRestaurantEvent.class);
        roundTrips(statusUpdated(), OrderStatusUpdatedEvent.class);
        roundTrips(delivered(), DeliveredEvent.class);
    }

    @Test
    void orderAcceptedMissingOccurredAtIsRefused() throws Exception {
        String json = withoutOccurredAt(accepted());
        assertTrue(assertThrows(EventBindingException.class, () -> binder.bind(json, OrderAcceptedEvent.class))
                .getMessage().contains("occurredAt"));
    }

    @Test
    void orderReadyMissingOccurredAtIsRefused() throws Exception {
        String json = withoutOccurredAt(ready());
        assertTrue(assertThrows(EventBindingException.class, () -> binder.bind(json, OrderReadyEvent.class))
                .getMessage().contains("occurredAt"));
    }

    @Test
    void driverAssignedMissingOccurredAtIsRefused() throws Exception {
        String json = withoutOccurredAt(assigned());
        assertTrue(assertThrows(EventBindingException.class, () -> binder.bind(json, DriverAssignedEvent.class))
                .getMessage().contains("occurredAt"));
    }

    @Test
    void driverAtRestaurantMissingOccurredAtIsRefused() throws Exception {
        String json = withoutOccurredAt(atRestaurant());
        assertTrue(assertThrows(EventBindingException.class, () -> binder.bind(json, DriverAtRestaurantEvent.class))
                .getMessage().contains("occurredAt"));
    }

    @Test
    void orderStatusUpdatedMissingOccurredAtIsRefused() throws Exception {
        String json = withoutOccurredAt(statusUpdated());
        assertTrue(assertThrows(EventBindingException.class, () -> binder.bind(json, OrderStatusUpdatedEvent.class))
                .getMessage().contains("occurredAt"));
    }

    @Test
    void deliveredMissingOccurredAtIsRefused() throws Exception {
        String json = withoutOccurredAt(delivered());
        assertTrue(assertThrows(EventBindingException.class, () -> binder.bind(json, DeliveredEvent.class))
                .getMessage().contains("occurredAt"));
    }
}
