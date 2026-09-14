package com.fooddelivery.common.event;

import java.util.UUID;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
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
public class OrderPaidEvent implements OrderScopedEvent {
    private UUID orderId;
    private UUID restaurantId;
    /**
     * Absent until 2026-09-10, so RestaurantOrder.customerId was permanently NULL for every order
     * the platform had ever taken.
     */
    private UUID customerId;
    private String customerName;
    /**
     * How the customer paid.
     *
     * <p>ORDER_PAID and ORDER_PLACED_COD were consumed by the same handler, which recorded neither
     * the event type nor this field -- so a restaurant could not tell an order the rider must
     * collect cash for from one already paid, and the delivery service could not require a declared
     * amount at handover.
     */
    private com.fooddelivery.common.enums.PaymentMethod paymentMethod;
    private Integer estimatedPrepTimeMinutes;
    private Double deliveryLat;
    private Double deliveryLng;
    private String deliveryAddress;
    private String itemsJson;
    private String pickupOtp;
    private String deliveryOtp;
    private java.math.BigDecimal totalAmount;
    private java.math.BigDecimal itemTotal;
    private java.math.BigDecimal restaurantPlatformFee;
    private java.math.BigDecimal restaurantDeliveryContribution;
    private java.math.BigDecimal platformBonus;
    private java.math.BigDecimal restaurantPayout;

    /** {@inheritDoc} */
    @Override
    public java.util.UUID orderUuid() {
        return orderId;
    }
}
