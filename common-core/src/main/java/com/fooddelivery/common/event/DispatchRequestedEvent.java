package com.fooddelivery.common.event;

import java.util.List;
import jakarta.validation.constraints.NotNull;

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
public class DispatchRequestedEvent {
    // The coordinates were enforced by `if (!rootNode.has("restaurantLat")) throw` in
    // DispatchEventConsumer -- a fail-fast the shape audit specifically had to special-case,
    // because a probe normally means a field is OPTIONAL. As constraints they say the same thing
    // declaratively, and the audit no longer has to guess.
    @NotNull(message = "orderId is required")
    private String orderId;

    @jakarta.validation.constraints.NotBlank(message = "dispatchCityId is required")
    private String dispatchCityId;

    @NotNull(message = "fleetSearchRadiusKm is required")
    @jakarta.validation.constraints.Positive(message = "fleetSearchRadiusKm must be positive")
    private Double fleetSearchRadiusKm;
    
    @jakarta.validation.constraints.NotNull(message = "restaurantLat is required to dispatch")
    private Double restaurantLat;
    
    @jakarta.validation.constraints.NotNull(message = "restaurantLng is required to dispatch")
    private Double restaurantLng;
    
    @jakarta.validation.constraints.NotNull(message = "deliveryLat is required to dispatch")
    private Double deliveryLat;
    
    @jakarta.validation.constraints.NotNull(message = "deliveryLng is required to dispatch")
    private Double deliveryLng;
    
    private String deliveryAddress;
    private List<String> excludedDriverIds;
}
