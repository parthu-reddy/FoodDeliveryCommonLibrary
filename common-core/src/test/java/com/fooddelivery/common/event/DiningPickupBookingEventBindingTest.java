package com.fooddelivery.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.config.JacksonConfig;
import com.fooddelivery.common.enums.PaymentMethod;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The DiningPickupBooking events (Phase 1) bind from their own serialized body, and each refuses a body missing its
 * aggregate id or its producer's instant.
 *
 * <p>The mapper comes from Boot's {@link JacksonAutoConfiguration} plus {@link JacksonConfig}, as in
 * {@code KitchenStaffChangedEventBindingTest}: a bare builder writes instants as epoch numbers, a format production
 * never sends.
 */
class DiningPickupBookingEventBindingTest {

    private static final Instant AT = Instant.parse("2026-11-03T13:30:00Z");

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

    private static List<Object> samples() {
        UUID order = UUID.randomUUID();
        UUID outlet = UUID.randomUUID();
        return List.of(
                OrderCollectedEvent.builder().orderId(order).restaurantId(outlet).collectedAt(AT)
                        .handedOverByUserId(UUID.randomUUID()).withoutCode(true).build(),
                OrderUnclaimedEvent.builder().orderId(order).restaurantId(outlet).unclaimedAt(AT).build(),
                PickupCustomerArrivedEvent.builder().orderId(order).restaurantId(outlet).arrivedAt(AT).build(),
                DiningTableChangedEvent.builder().tableId(UUID.randomUUID()).outletId(outlet).label("T4").seats(4)
                        .active(true).bookable(true).version(3).occurredAt(AT).build(),
                DiningAssistanceRequestedEvent.builder().requestId(UUID.randomUUID()).sessionId(UUID.randomUUID())
                        .outletId(outlet).tableId(UUID.randomUUID()).guestUserId(UUID.randomUUID()).reason("WATER")
                        .requestedAt(AT).build(),
                ReservationChangedEvent.builder().reservationId(UUID.randomUUID()).outletId(outlet)
                        .customerUserId(UUID.randomUUID()).tableId(UUID.randomUUID()).party(4).startsAt(AT)
                        .blocksUntil(AT.plusSeconds(7200)).status("CONFIRMED").version(2).occurredAt(AT).build(),
                ReservationPaymentEvent.builder().referenceId(UUID.randomUUID().toString()).gatewayOrderId("order_Rz1")
                        .amount(new BigDecimal("2000.00")).gatewayName("RAZORPAY").paymentMethod(PaymentMethod.UPI)
                        .occurredAt(AT).build(),
                OutletServiceModesChangedEvent.builder().outletId(outlet).pickupEnabled(true).pickupUnclaimedMinutes(120).geofenceMetres(100)
                        .version(1).occurredAt(AT).build());
    }

    @Test
    void everySerializedEventBindsBack() throws Exception {
        for (Object sent : samples()) {
            String json = mapper.writeValueAsString(sent);
            assertTrue(json.contains("\"2026-11-03T13:30:00Z\""), json);
            assertEquals(sent, binder.bind(json, sent.getClass()), sent.getClass().getSimpleName());
        }
    }

    @Test
    void theOrderScopedOnesExposeTheirOrderIdWithoutAddingItToTheWire() throws Exception {
        for (Object sent : samples()) {
            if (sent instanceof OrderScopedEvent scoped) {
                assertNotNull(scoped.orderUuid(), sent.getClass().getSimpleName());
                assertFalse(mapper.writeValueAsString(sent).contains("orderUuid"), sent.getClass().getSimpleName());
            }
        }
    }

    @Test
    void aMissingAggregateIdIsRefused() throws Exception {
        assertRefused(OrderCollectedEvent.builder().restaurantId(UUID.randomUUID()).collectedAt(AT)
                .handedOverByUserId(UUID.randomUUID()).build());
        assertRefused(OrderUnclaimedEvent.builder().restaurantId(UUID.randomUUID()).unclaimedAt(AT).build());
        assertRefused(PickupCustomerArrivedEvent.builder().restaurantId(UUID.randomUUID()).arrivedAt(AT).build());
        assertRefused(DiningTableChangedEvent.builder().outletId(UUID.randomUUID()).label("T4").occurredAt(AT).build());
        assertRefused(DiningAssistanceRequestedEvent.builder().requestId(UUID.randomUUID()).outletId(UUID.randomUUID())
                .tableId(UUID.randomUUID()).guestUserId(UUID.randomUUID()).reason("BILL").requestedAt(AT).build());
        assertRefused(ReservationChangedEvent.builder().outletId(UUID.randomUUID()).customerUserId(UUID.randomUUID())
                .tableId(UUID.randomUUID()).party(2).startsAt(AT).blocksUntil(AT).status("HELD").occurredAt(AT).build());
        assertRefused(ReservationPaymentEvent.builder().gatewayOrderId("order_Rz1").amount(BigDecimal.TEN)
                .occurredAt(AT).build());
        assertRefused(OutletServiceModesChangedEvent.builder().pickupUnclaimedMinutes(120).geofenceMetres(100).occurredAt(AT).build());
    }

    @Test
    void aMissingProducerInstantIsRefused() throws Exception {
        for (Object sent : samples()) {
            String json = mapper.writeValueAsString(sent).replace("\"2026-11-03T13:30:00Z\"", "null");
            assertThrows(EventBindingException.class, () -> binder.bind(json, sent.getClass()),
                    sent.getClass().getSimpleName());
        }
    }

    @Test
    void outOfRangeValuesAreRefused() throws Exception {
        assertRefused(OutletServiceModesChangedEvent.builder().outletId(UUID.randomUUID()).pickupUnclaimedMinutes(30).geofenceMetres(100)
                .occurredAt(AT).build());
        assertRefused(OutletServiceModesChangedEvent.builder().outletId(UUID.randomUUID()).pickupUnclaimedMinutes(120).geofenceMetres(49)
                .occurredAt(AT).build());
        assertRefused(ReservationChangedEvent.builder().reservationId(UUID.randomUUID()).outletId(UUID.randomUUID())
                .customerUserId(UUID.randomUUID()).tableId(UUID.randomUUID()).party(0).startsAt(AT).blocksUntil(AT)
                .status("HELD").occurredAt(AT).build());
        assertRefused(ReservationPaymentEvent.builder().referenceId(UUID.randomUUID().toString()).gatewayOrderId("order_Rz1")
                .amount(BigDecimal.ZERO).occurredAt(AT).build());
        assertRefused(DiningTableChangedEvent.builder().tableId(UUID.randomUUID()).outletId(UUID.randomUUID()).label("T4")
                .seats(0).occurredAt(AT).build());
    }

    private void assertRefused(Object event) throws Exception {
        String json = mapper.writeValueAsString(event);
        assertThrows(EventBindingException.class, () -> binder.bind(json, event.getClass()), json);
    }
}
