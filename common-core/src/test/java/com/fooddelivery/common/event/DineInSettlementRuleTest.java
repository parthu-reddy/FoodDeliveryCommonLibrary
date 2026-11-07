package com.fooddelivery.common.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.enums.DineInSettlement;
import com.fooddelivery.common.enums.FulfilmentType;
import com.fooddelivery.common.enums.PaymentMethod;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * DineInPhoneAndCounter D5/D6: a payment method and a settlement agree with the fulfilment type on both order events that
 * carry them. Only the settlement rule is asserted here; FulfilmentShapeTest owns the table and dispatch fields.
 */
class DineInSettlementRuleTest {

    private static final String RULE = "settlementConsistent";
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> violated(Object event) {
        return validator.validate(event).stream().map(ConstraintViolation::getPropertyPath).map(Object::toString)
                .collect(Collectors.toSet());
    }

    private static OrderAcceptedEvent accepted(FulfilmentType type, PaymentMethod method, DineInSettlement settlement) {
        return OrderAcceptedEvent.builder().orderId(UUID.randomUUID().toString()).restaurantId(UUID.randomUUID().toString())
                .restaurantLat(12.9).restaurantLng(77.6).fulfilmentType(type).paymentMethod(method).settlement(settlement)
                .occurredAt(Instant.parse("2026-10-31T10:00:00Z")).build();
    }

    @Test
    void theRuleTable() {
        assertThat(DineInSettlement.consistent(FulfilmentType.DELIVERY, PaymentMethod.UPI, null)).isTrue();
        assertThat(DineInSettlement.consistent(FulfilmentType.DELIVERY, null, null)).isFalse();
        assertThat(DineInSettlement.consistent(FulfilmentType.DELIVERY, PaymentMethod.UPI, DineInSettlement.APP)).isFalse();
        assertThat(DineInSettlement.consistent(FulfilmentType.DINE_IN, PaymentMethod.CARD, DineInSettlement.APP)).isTrue();
        assertThat(DineInSettlement.consistent(FulfilmentType.DINE_IN, null, DineInSettlement.RESTAURANT)).isTrue();
        assertThat(DineInSettlement.consistent(FulfilmentType.DINE_IN, null, DineInSettlement.APP)).isFalse();
        assertThat(DineInSettlement.consistent(FulfilmentType.DINE_IN, PaymentMethod.WALLET, DineInSettlement.RESTAURANT)).isFalse();
        assertThat(DineInSettlement.consistent(FulfilmentType.DINE_IN, PaymentMethod.UPI, null)).isFalse();
        assertThat(DineInSettlement.consistent(FulfilmentType.PICKUP, PaymentMethod.UPI, null)).isTrue();
        assertThat(DineInSettlement.consistent(FulfilmentType.PICKUP, null, null)).isFalse();
        assertThat(DineInSettlement.consistent(FulfilmentType.PICKUP, PaymentMethod.UPI, DineInSettlement.APP)).isFalse();
        assertThat(DineInSettlement.consistent(FulfilmentType.PICKUP, null, DineInSettlement.RESTAURANT)).isFalse();
    }

    @Test
    void aMissingFulfilmentTypeIsLeftToItsOwnNotNull() {
        assertThat(DineInSettlement.consistent(null, null, null)).isTrue();
    }

    @Test
    void aRestaurantSettledDineInAcceptedEventNeedsNoMethod() {
        assertThat(violated(accepted(FulfilmentType.DINE_IN, null, DineInSettlement.RESTAURANT))).doesNotContain(RULE);
    }

    @Test
    void anAppDineInAcceptedEventNeedsItsMethod() {
        assertThat(violated(accepted(FulfilmentType.DINE_IN, null, DineInSettlement.APP))).contains(RULE);
    }

    @Test
    void aDeliveryAcceptedEventNeedsAMethodAndNoSettlement() {
        assertThat(violated(accepted(FulfilmentType.DELIVERY, null, null))).contains(RULE);
        assertThat(violated(accepted(FulfilmentType.DELIVERY, PaymentMethod.CARD, DineInSettlement.APP))).contains(RULE);
        assertThat(violated(accepted(FulfilmentType.DELIVERY, PaymentMethod.CARD, null))).doesNotContain(RULE);
    }

    @Test
    void aDineInPaidEventWithoutASettlementIsRefused() {
        OrderPaidEvent event = OrderPaidEvent.builder().orderId(UUID.randomUUID()).fulfilmentType(FulfilmentType.DINE_IN)
                .paymentMethod(PaymentMethod.UPI).build();
        assertThat(violated(event)).contains(RULE);
    }

    @Test
    void aRestaurantSettledPaidEventWithAMethodIsRefused() {
        OrderPaidEvent event = OrderPaidEvent.builder().orderId(UUID.randomUUID()).fulfilmentType(FulfilmentType.DINE_IN)
                .settlement(DineInSettlement.RESTAURANT).paymentMethod(PaymentMethod.UPI).build();
        assertThat(violated(event)).contains(RULE);
    }

    @Test
    void onlyADineInSnapshotNoGatewayTookMoneyForIsAConfirmation() {
        assertThat(OrderPaidEvent.builder().fulfilmentType(FulfilmentType.DINE_IN).settlement(DineInSettlement.CREDIT).build()
                .confirmedWithoutGateway()).isTrue();
        assertThat(OrderPaidEvent.builder().fulfilmentType(FulfilmentType.DINE_IN).settlement(DineInSettlement.RESTAURANT).build()
                .confirmedWithoutGateway()).isTrue();
        assertThat(OrderPaidEvent.builder().fulfilmentType(FulfilmentType.DINE_IN).settlement(DineInSettlement.APP)
                .paymentMethod(PaymentMethod.UPI).build().confirmedWithoutGateway()).isFalse();
        assertThat(OrderPaidEvent.builder().fulfilmentType(FulfilmentType.DELIVERY).paymentMethod(PaymentMethod.UPI).build()
                .confirmedWithoutGateway()).isFalse();
        assertThat(OrderPaidEvent.builder().fulfilmentType(FulfilmentType.PICKUP).paymentMethod(PaymentMethod.UPI).build()
                .confirmedWithoutGateway()).isFalse();
    }

    @Test
    void theEventTypeMustMatchTheSnapshot() {
        OrderPaidEvent confirmed = OrderPaidEvent.builder().fulfilmentType(FulfilmentType.DINE_IN).settlement(DineInSettlement.RESTAURANT).build();
        OrderPaidEvent paidInApp = OrderPaidEvent.builder().fulfilmentType(FulfilmentType.DINE_IN).settlement(DineInSettlement.APP)
                .paymentMethod(PaymentMethod.UPI).build();
        confirmed.requireConsistentWith(com.fooddelivery.common.constants.EventType.DINE_IN_ORDER_CONFIRMED);
        paidInApp.requireConsistentWith(com.fooddelivery.common.constants.EventType.ORDER_PAID);
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> confirmed.requireConsistentWith(com.fooddelivery.common.constants.EventType.ORDER_PAID))
                .isInstanceOf(EventBindingException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> paidInApp.requireConsistentWith(com.fooddelivery.common.constants.EventType.DINE_IN_ORDER_CONFIRMED))
                .isInstanceOf(EventBindingException.class);
    }

    @Test
    void theRuleIsNotOnTheWire() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        var json = mapper.valueToTree(accepted(FulfilmentType.DINE_IN, null, DineInSettlement.RESTAURANT));
        assertThat(json.has(RULE)).isFalse();
        assertThat(json.get("settlement").asText()).isEqualTo("RESTAURANT");
        assertThat(json.has("paymentMethod") && !json.get("paymentMethod").isNull()).isFalse();
    }
}
