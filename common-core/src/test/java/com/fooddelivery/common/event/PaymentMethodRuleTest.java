package com.fooddelivery.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.constants.EventType;
import com.fooddelivery.common.enums.FulfilmentType;
import com.fooddelivery.common.enums.PaymentMethod;
import com.fooddelivery.common.enums.PaymentMethodRule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * TableBill_2026-11-18 D4: DELIVERY and PICKUP orders are prepaid in the app and carry a payment method; a DINE_IN round is
 * paid on its table's bill and carries none, but names its dining session. Only this rule is asserted here;
 * FulfilmentShapeTest owns the table and dispatch fields.
 */
class PaymentMethodRuleTest {

    private static final String RULE = "paymentMethodConsistent";
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> violated(Object event) {
        return validator.validate(event).stream().map(ConstraintViolation::getPropertyPath).map(Object::toString)
                .collect(Collectors.toSet());
    }

    private static OrderAcceptedEvent accepted(FulfilmentType type, PaymentMethod method, UUID session) {
        return OrderAcceptedEvent.builder().orderId(UUID.randomUUID().toString()).restaurantId(UUID.randomUUID().toString())
                .restaurantLat(12.9).restaurantLng(77.6).fulfilmentType(type).paymentMethod(method).diningSessionId(session)
                .occurredAt(Instant.parse("2026-11-19T10:00:00Z")).build();
    }

    // ---- the rule table: each fulfilment type with and without a method

    @Test
    void aDeliveryWithAMethodIsConsistent() {
        assertThat(PaymentMethodRule.consistent(FulfilmentType.DELIVERY, PaymentMethod.UPI)).isTrue();
    }

    @Test
    void aDeliveryWithoutAMethodIsNot() {
        assertThat(PaymentMethodRule.consistent(FulfilmentType.DELIVERY, null)).isFalse();
    }

    @Test
    void aPickupWithAMethodIsConsistent() {
        assertThat(PaymentMethodRule.consistent(FulfilmentType.PICKUP, PaymentMethod.CARD)).isTrue();
    }

    @Test
    void aPickupWithoutAMethodIsNot() {
        assertThat(PaymentMethodRule.consistent(FulfilmentType.PICKUP, null)).isFalse();
    }

    @Test
    void aDineInRoundWithoutAMethodIsConsistent() {
        assertThat(PaymentMethodRule.consistent(FulfilmentType.DINE_IN, null)).isTrue();
    }

    @Test
    void aDineInRoundWithAMethodIsNot() {
        for (PaymentMethod method : PaymentMethod.values()) {
            assertThat(PaymentMethodRule.consistent(FulfilmentType.DINE_IN, method)).as(method.name()).isFalse();
        }
    }

    @Test
    void aMissingFulfilmentTypeIsLeftToItsOwnNotNull() {
        assertThat(PaymentMethodRule.consistent(null, null)).isTrue();
    }

    // ---- on the events

    @Test
    void aDineInRoundNeedsItsSessionAndNoMethod() {
        UUID session = UUID.randomUUID();
        assertThat(violated(accepted(FulfilmentType.DINE_IN, null, session))).doesNotContain(RULE);
        assertThat(violated(accepted(FulfilmentType.DINE_IN, null, null))).contains(RULE);
        assertThat(violated(accepted(FulfilmentType.DINE_IN, PaymentMethod.UPI, session))).contains(RULE);
    }

    @Test
    void aDeliveryNeedsAMethodAndNoSession() {
        assertThat(violated(accepted(FulfilmentType.DELIVERY, PaymentMethod.CARD, null))).doesNotContain(RULE);
        assertThat(violated(accepted(FulfilmentType.DELIVERY, null, null))).contains(RULE);
        assertThat(violated(accepted(FulfilmentType.DELIVERY, PaymentMethod.CARD, UUID.randomUUID()))).contains(RULE);
    }

    @Test
    void thePaidEventFollowsTheSameRule() {
        assertThat(violated(OrderPaidEvent.builder().orderId(UUID.randomUUID()).fulfilmentType(FulfilmentType.DINE_IN)
                .paymentMethod(PaymentMethod.UPI).diningSessionId(UUID.randomUUID()).build())).contains(RULE);
        assertThat(violated(OrderPaidEvent.builder().orderId(UUID.randomUUID()).fulfilmentType(FulfilmentType.DINE_IN)
                .build())).contains(RULE);
        assertThat(violated(OrderPaidEvent.builder().orderId(UUID.randomUUID()).fulfilmentType(FulfilmentType.PICKUP)
                .paymentMethod(PaymentMethod.UPI).diningSessionId(UUID.randomUUID()).build())).contains(RULE);
        assertThat(violated(OrderPaidEvent.builder().orderId(UUID.randomUUID()).fulfilmentType(FulfilmentType.DINE_IN)
                .diningSessionId(UUID.randomUUID()).build())).doesNotContain(RULE);
    }

    @Test
    void everyDineInRoundIsAConfirmationAndNothingElseIs() {
        assertThat(OrderPaidEvent.builder().fulfilmentType(FulfilmentType.DINE_IN).build().confirmedWithoutGateway()).isTrue();
        assertThat(OrderPaidEvent.builder().fulfilmentType(FulfilmentType.DELIVERY).build().confirmedWithoutGateway()).isFalse();
        assertThat(OrderPaidEvent.builder().fulfilmentType(FulfilmentType.PICKUP).build().confirmedWithoutGateway()).isFalse();
        assertThat(OrderPaidEvent.builder().build().confirmedWithoutGateway()).isFalse();
    }

    @Test
    void theEventTypeMustMatchTheSnapshot() {
        OrderPaidEvent round = OrderPaidEvent.builder().fulfilmentType(FulfilmentType.DINE_IN).build();
        OrderPaidEvent delivery = OrderPaidEvent.builder().fulfilmentType(FulfilmentType.DELIVERY).build();
        round.requireConsistentWith(EventType.DINE_IN_ORDER_CONFIRMED);
        delivery.requireConsistentWith(EventType.ORDER_PAID);
        assertThatThrownBy(() -> round.requireConsistentWith(EventType.ORDER_PAID)).isInstanceOf(EventBindingException.class);
        assertThatThrownBy(() -> delivery.requireConsistentWith(EventType.DINE_IN_ORDER_CONFIRMED))
                .isInstanceOf(EventBindingException.class);
    }

    @Test
    void theRuleIsNotOnTheWireAndNoSettlementIs() {
        UUID session = UUID.randomUUID();
        var json = new ObjectMapper().findAndRegisterModules().valueToTree(accepted(FulfilmentType.DINE_IN, null, session));
        assertThat(json.has(RULE)).isFalse();
        assertThat(json.has("settlement")).isFalse();
        assertThat(json.get("diningSessionId").asText()).isEqualTo(session.toString());
    }
}
