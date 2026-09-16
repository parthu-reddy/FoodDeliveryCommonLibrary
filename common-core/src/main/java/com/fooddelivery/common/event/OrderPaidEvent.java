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
    @jakarta.validation.constraints.NotNull(message = "orderId is required")
    private UUID orderId;
    @jakarta.validation.constraints.NotNull(message = "restaurantId is required")
    private UUID restaurantId;
    /**
     * Absent until 2026-09-10, so RestaurantOrder.customerId was permanently NULL for every order
     * the platform had ever taken.
     */
    @jakarta.validation.constraints.NotNull(message = "customerId is required")
    private UUID customerId;
    private String customerName;
    /** How the customer paid. All accepted values represent completed prepaid methods. */
    @jakarta.validation.constraints.NotNull(message = "paymentMethod is required")
    private com.fooddelivery.common.enums.PaymentMethod paymentMethod;
    private Integer estimatedPrepTimeMinutes;
    @jakarta.validation.constraints.NotNull(message = "deliveryLat is required")
    private Double deliveryLat;
    @jakarta.validation.constraints.NotNull(message = "deliveryLng is required")
    private Double deliveryLng;
    private String deliveryAddress;
    private String itemsJson;
    @jakarta.validation.constraints.NotBlank(message = "pickupOtp is required")
    @jakarta.validation.constraints.Pattern(regexp = "\\d{6}", message = "pickupOtp must be six digits")
    private String pickupOtp;
    @jakarta.validation.constraints.NotBlank(message = "deliveryOtp is required")
    @jakarta.validation.constraints.Pattern(regexp = "\\d{6}", message = "deliveryOtp must be six digits")
    private String deliveryOtp;
    @jakarta.validation.constraints.NotBlank(message = "dispatchCityId is required")
    private String dispatchCityId;
    @jakarta.validation.constraints.NotNull(message = "fleetSearchRadiusKm is required")
    @jakarta.validation.constraints.Positive(message = "fleetSearchRadiusKm must be positive")
    private Double fleetSearchRadiusKm;
    @jakarta.validation.constraints.NotNull(message = "totalAmount is required")
    @jakarta.validation.constraints.Positive(message = "totalAmount must be positive")
    private java.math.BigDecimal totalAmount;
    @jakarta.validation.constraints.NotNull(message = "itemTotal is required")
    @jakarta.validation.constraints.PositiveOrZero(message = "itemTotal must not be negative")
    private java.math.BigDecimal itemTotal;
    @jakarta.validation.constraints.NotNull(message = "restaurantPlatformFee is required")
    @jakarta.validation.constraints.PositiveOrZero(message = "restaurantPlatformFee must not be negative")
    private java.math.BigDecimal restaurantPlatformFee;
    @jakarta.validation.constraints.NotNull(message = "restaurantDeliveryContribution is required")
    @jakarta.validation.constraints.PositiveOrZero(message = "restaurantDeliveryContribution must not be negative")
    private java.math.BigDecimal restaurantDeliveryContribution;
    @jakarta.validation.constraints.NotNull(message = "platformBonus is required")
    @jakarta.validation.constraints.PositiveOrZero(message = "platformBonus must not be negative")
    private java.math.BigDecimal platformBonus;
    @jakarta.validation.constraints.NotNull(message = "restaurantPayout is required")
    @jakarta.validation.constraints.PositiveOrZero(message = "restaurantPayout must not be negative")
    private java.math.BigDecimal restaurantPayout;

    /** {@inheritDoc} */
    @Override
    public java.util.UUID orderUuid() {
        return orderId;
    }
}
