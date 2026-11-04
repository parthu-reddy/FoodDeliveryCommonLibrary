package com.fooddelivery.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.config.JacksonConfig;
import com.fooddelivery.common.enums.DineInSettlement;
import com.fooddelivery.common.enums.FulfilmentType;
import com.fooddelivery.common.enums.PaymentMethod;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The order events' shape follows fulfilmentType (WaiterProfile_2026-10-29 Phase 1): DELIVERY needs the whole dispatch
 * scope and no table, DINE_IN needs a table and no dispatch scope, and a missing type is refused.
 */
class FulfilmentShapeTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static OrderPaidEvent.OrderPaidEventBuilder paid() {
        return OrderPaidEvent.builder().orderId(UUID.randomUUID()).restaurantId(UUID.randomUUID()).customerId(UUID.randomUUID())
                .paymentMethod(PaymentMethod.UPI)
                .items(List.of(new OrderLineSnapshot(UUID.randomUUID(), UUID.randomUUID(), "Dosa", 1, new BigDecimal("80.00"),
                        UUID.randomUUID(), "South", true)))
                .totalAmount(new BigDecimal("90.00")).itemTotal(new BigDecimal("80.00")).restaurantPlatformFee(new BigDecimal("8.00"))
                .restaurantDeliveryContribution(BigDecimal.ZERO).platformBonus(BigDecimal.ZERO).restaurantPayout(new BigDecimal("72.00"));
    }

    private static OrderPaidEvent.OrderPaidEventBuilder withDispatch(OrderPaidEvent.OrderPaidEventBuilder b) {
        return b.deliveryLat(12.97).deliveryLng(77.59).pickupOtp("123456").deliveryOtp("567890").dispatchCityId("BLR")
                .fleetSearchRadiusKm(5.0);
    }

    private static OrderAcceptedEvent.OrderAcceptedEventBuilder accepted() {
        return OrderAcceptedEvent.builder().orderId(UUID.randomUUID().toString()).restaurantId(UUID.randomUUID().toString())
                .restaurantLat(12.9).restaurantLng(77.6).paymentMethod(PaymentMethod.CARD).occurredAt(Instant.parse("2026-10-29T10:00:00Z"));
    }

    /** {field: message} of every violation. */
    private <T> Map<String, String> violations(T event) {
        Map<String, String> out = new TreeMap<>();
        for (ConstraintViolation<T> v : validator.validate(event)) {
            out.put(v.getPropertyPath().toString(), v.getMessage());
        }
        return out;
    }

    @Test
    void aDeliveryWithItsDispatchScopeIsValid() {
        assertThat(violations(withDispatch(paid()).fulfilmentType(FulfilmentType.DELIVERY).build())).isEmpty();
    }

    @Test
    void aDeliveryMissingItsDispatchScopeNamesEveryField() {
        assertThat(violations(paid().fulfilmentType(FulfilmentType.DELIVERY).dispatchCityId(" ").build())).containsExactlyInAnyOrderEntriesOf(Map.of(
                "deliveryLat", "deliveryLat is required", "deliveryLng", "deliveryLng is required",
                "pickupOtp", "pickupOtp is required", "deliveryOtp", "deliveryOtp is required",
                "dispatchCityId", "dispatchCityId is required", "fleetSearchRadiusKm", "fleetSearchRadiusKm is required"));
    }

    @Test
    void aDeliveryCarriesNoTable() {
        assertThat(violations(withDispatch(paid()).fulfilmentType(FulfilmentType.DELIVERY).tableLabel("T4").build()))
                .containsExactlyEntriesOf(Map.of("tableLabel", "tableLabel must be absent for DELIVERY"));
    }

    @Test
    void aDineInNeedsATableAndNoDispatchScope() {
        assertThat(violations(paid().fulfilmentType(FulfilmentType.DINE_IN).tableLabel("T4").settlement(DineInSettlement.APP).build())).isEmpty();
        assertThat(violations(paid().fulfilmentType(FulfilmentType.DINE_IN).settlement(DineInSettlement.APP).build()))
                .containsExactlyEntriesOf(Map.of("tableLabel", "tableLabel is required for DINE_IN"));
        assertThat(violations(withDispatch(paid()).fulfilmentType(FulfilmentType.DINE_IN).tableLabel("T4").settlement(DineInSettlement.APP).build()))
                .containsOnlyKeys("deliveryLat", "deliveryLng", "pickupOtp", "deliveryOtp", "dispatchCityId", "fleetSearchRadiusKm")
                .allSatisfy((field, message) -> assertThat(message).isEqualTo(field + " must be absent for DINE_IN"));
    }

    @Test
    void aPickupCarriesNoTableAndNoDispatchScope() {
        assertThat(violations(paid().fulfilmentType(FulfilmentType.PICKUP).customerPhoneLast4("4321").build())).isEmpty();
        assertThat(violations(paid().fulfilmentType(FulfilmentType.PICKUP).customerPhoneLast4("4321").tableLabel("T4").build()))
                .containsExactlyEntriesOf(Map.of("tableLabel", "tableLabel must be absent for PICKUP"));
        assertThat(violations(withDispatch(paid()).fulfilmentType(FulfilmentType.PICKUP).customerPhoneLast4("4321").build()))
                .containsOnlyKeys("deliveryLat", "deliveryLng", "pickupOtp", "deliveryOtp", "dispatchCityId", "fleetSearchRadiusKm")
                .allSatisfy((field, message) -> assertThat(message).isEqualTo(field + " must be absent for PICKUP"));
        assertThat(violations(accepted().fulfilmentType(FulfilmentType.PICKUP).build())).isEmpty();
    }

    @Test
    void onlyAPickupCarriesThePhoneLast4() {
        assertThat(violations(paid().fulfilmentType(FulfilmentType.PICKUP).build()))
                .containsExactlyEntriesOf(Map.of("phoneLast4Consistent", "customerPhoneLast4 is required for PICKUP and absent otherwise"));
        assertThat(violations(paid().fulfilmentType(FulfilmentType.PICKUP).customerPhoneLast4("98765").build()))
                .containsExactlyEntriesOf(Map.of("customerPhoneLast4", "customerPhoneLast4 must be four digits"));
        assertThat(violations(withDispatch(paid()).fulfilmentType(FulfilmentType.DELIVERY).customerPhoneLast4("4321").build()))
                .containsExactlyEntriesOf(Map.of("phoneLast4Consistent", "customerPhoneLast4 is required for PICKUP and absent otherwise"));
    }

    @Test
    void aMissingFulfilmentTypeIsRefusedOnBothEvents() {
        assertThat(violations(withDispatch(paid()).build())).containsExactlyEntriesOf(Map.of("fulfilmentType", "fulfilmentType is required"));
        assertThat(violations(accepted().build())).containsExactlyEntriesOf(Map.of("fulfilmentType", "fulfilmentType is required"));
    }

    @Test
    void theAcceptedEventFollowsTheSameRule() {
        assertThat(violations(accepted().fulfilmentType(FulfilmentType.DINE_IN).tableLabel("T4").settlement(DineInSettlement.APP).build())).isEmpty();
        assertThat(violations(accepted().fulfilmentType(FulfilmentType.DELIVERY).build()))
                .containsOnlyKeys("deliveryLat", "deliveryLng", "pickupOtp", "deliveryOtp", "dispatchCityId", "fleetSearchRadiusKm");
    }

    @Test
    void deliveryScopeIsNotOnTheWire() throws Exception {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new JacksonConfig().jsonCustomizer().customize(builder);
        ObjectMapper mapper = builder.build();
        var json = mapper.valueToTree(withDispatch(paid()).fulfilmentType(FulfilmentType.DELIVERY).build());
        assertThat(json.has("deliveryScope")).isFalse();
        assertThat(json.get("fulfilmentType").asText()).isEqualTo("DELIVERY");
    }

    @Test
    void kitchenProgressLinesCannotOverstateTheQuantity() {
        var line = KitchenItemProgressChangedEvent.Line.builder().orderItemId(UUID.randomUUID()).quantity(2).readyQuantity(2).voidQuantity(1).build();
        var event = KitchenItemProgressChangedEvent.builder().orderId(UUID.randomUUID()).outletId(UUID.randomUUID()).version(3)
                .lines(List.of(line)).occurredAt(Instant.parse("2026-10-29T10:00:00Z")).build();
        assertThat(violations(event)).containsExactlyEntriesOf(Map.of("lines[0].withinQuantity", "readyQuantity + voidQuantity must not exceed quantity"));
        line.setVoidQuantity(0);
        assertThat(violations(event)).isEmpty();
    }
}
