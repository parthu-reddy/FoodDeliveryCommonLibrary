package com.fooddelivery.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * order-events / READY_CONTESTED: a rider at the outlet, whose arrival was geo-verified, says the food the restaurant marked
 * ready is not ready yet (DelayPenalties D14, scenario B4). Accepted once per rider stretch. A contested Ready makes the
 * late-food and kept-waiting rules measure to the pickup instead of the Ready tap.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ReadyContestedEvent implements OrderScopedEvent {
    @NotNull
    private String orderId;
    @NotNull
    private String driverId;

    /** When the rider tapped "Food not ready yet", on the delivery service's clock. */
    @NotNull(message = "occurredAt is required")
    private java.time.Instant occurredAt;

    @Override
    public java.util.UUID orderUuid() {
        return orderId == null || orderId.isBlank() ? null : java.util.UUID.fromString(orderId);
    }
}
