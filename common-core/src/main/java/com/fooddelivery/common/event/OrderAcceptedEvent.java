package com.fooddelivery.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fooddelivery.common.enums.FulfilmentType;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/**
 * Wire shape tolerates fields it does not declare -- {@code eventType} above all.
 *
 * <p>Repeating eventType in the body is this platform's convention: EventPayloadUtils records 28
 * production call sites and 6 message contracts doing it, WebhookProcessingService adds it with
 * {@code valueToTree(event).put("eventType", ...)}, and OutboxProcessor enforces only that it must
 * not CONTRADICT the outbox row (ADR 002). So the class genuinely receives a field it has no
 * component for.
 *
 * <p>Declared here rather than left to the ObjectMapper's global setting. Boot's mapper disables
 * FAIL_ON_UNKNOWN_PROPERTIES, but a plain {@code new ObjectMapper()} does not, and several test
 * harnesses build one -- PaymentEventConsumerRefundIdempotencyTest threw
 * UnrecognizedPropertyException on exactly this field. Binding must not depend on which mapper
 * happens to be wired.
 */
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
@FulfilmentShape
public class OrderAcceptedEvent implements OrderScopedEvent, FulfilmentScopedEvent {
    @NotNull(message = "orderId is required")
    private String orderId;
    @jakarta.validation.constraints.NotBlank(message = "restaurantId is required")
    private String restaurantId;
    @NotNull(message = "restaurantLat is required")
    private Double restaurantLat;
    @NotNull(message = "restaurantLng is required")
    private Double restaurantLng;
    private Long estimatedCompletionTime;
    private Integer estimatedPrepTimeMinutes;
    /** Decides which fields below are present: the delivery scope for DELIVERY, the table for DINE_IN ({@link FulfilmentShape}). */
    @NotNull(message = "fulfilmentType is required")
    private FulfilmentType fulfilmentType;
    /** The table the waiter entered (DINE_IN only). */
    @jakarta.validation.constraints.Size(max = 32, message = "tableLabel must be at most 32 characters")
    private String tableLabel;
    /** Delivery scope (required for DELIVERY, absent for DINE_IN): deliveryLat, deliveryLng, both OTPs, dispatch city, radius. */
    private Double deliveryLat;
    private Double deliveryLng;
    private String deliveryAddress;
    @jakarta.validation.constraints.Pattern(regexp = "\\d{6}", message = "pickupOtp must be six digits")
    private String pickupOtp;
    @jakarta.validation.constraints.Pattern(regexp = "\\d{6}", message = "deliveryOtp must be six digits")
    private String deliveryOtp;
    private String dispatchCityId;
    @jakarta.validation.constraints.Positive(message = "fleetSearchRadiusKm must be positive")
    private Double fleetSearchRadiusKm;
    private String customerName;
    /** The order's prepaid method; absent exactly when a DINE_IN order is settled at the restaurant ({@link #isSettlementConsistent}). */
    private com.fooddelivery.common.enums.PaymentMethod paymentMethod;
    /** DINE_IN only: APP or RESTAURANT, as on the order's ORDER_PAID / DINE_IN_ORDER_CONFIRMED. */
    private com.fooddelivery.common.enums.DineInSettlement settlement;


    /**
     * When this happened, on the producer's clock, stamped in the transaction that wrote the outbox row. Consumers store
     * this value and never their own receive time, so Kafka lag, a retry back-off or a DLT replay cannot move a milestone
     * (RandomDocuments/DelayPenalties_2026-10-25, Phase 1).
     */
    @NotNull(message = "occurredAt is required")
    private java.time.Instant occurredAt;
    /** {@link com.fooddelivery.common.enums.DineInSettlement#consistent}, as on OrderPaidEvent. */
    @jakarta.validation.constraints.AssertTrue(message = "paymentMethod and settlement do not match the fulfilment type")
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isSettlementConsistent() {
        return com.fooddelivery.common.enums.DineInSettlement.consistent(fulfilmentType, paymentMethod, settlement);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The wire field is a String; consumers want the UUID.
     */
    @Override
    public java.util.UUID orderUuid() {
        return orderId == null || orderId.isBlank() ? null : java.util.UUID.fromString(orderId);
    }

    /** {@inheritDoc} */
    @Override
    public java.util.Map<String, Object> deliveryScope() {
        java.util.Map<String, Object> scope = new java.util.LinkedHashMap<>();
        scope.put("deliveryLat", deliveryLat);
        scope.put("deliveryLng", deliveryLng);
        scope.put("pickupOtp", pickupOtp);
        scope.put("deliveryOtp", deliveryOtp);
        scope.put("dispatchCityId", dispatchCityId);
        scope.put("fleetSearchRadiusKm", fleetSearchRadiusKm);
        return scope;
    }
}
