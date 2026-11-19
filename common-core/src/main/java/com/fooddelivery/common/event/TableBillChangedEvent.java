package com.fooddelivery.common.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fooddelivery.common.enums.TableBillStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A table bill's amount due, amount paid or status changed (order-events, key = sessionId; TableBill_2026-11-18 D3, D12).
 * State-carrying: each event is the bill's whole money state, not a delta.
 *
 * <p>Producer: order-service, in the transaction that changed the bill (a round placed or voided, credit applied, a
 * payment recorded or captured or refunded). Consumer: the dining module (RestaurantApplication), for the close rule and
 * the floor tile. {@code sessionId} is the dining session, which is also the bill's id. The consumer applies an event
 * only when {@code version} is newer than the one it holds, so a replay or out-of-order delivery cannot roll the bill back.
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class TableBillChangedEvent {
    @NotNull(message = "sessionId is required")
    private UUID sessionId;
    @NotNull(message = "outletId is required")
    private UUID outletId;
    /** What the bill comes to (TableBillMath.due): its rounds after voids, less booking credit. */
    @NotNull(message = "amountDue is required")
    @PositiveOrZero(message = "amountDue must not be negative")
    private BigDecimal amountDue;
    /** The sum of the bill's succeeded payments. */
    @NotNull(message = "amountPaid is required")
    @PositiveOrZero(message = "amountPaid must not be negative")
    private BigDecimal amountPaid;
    @NotNull(message = "status is required")
    private TableBillStatus status;
    @NotNull(message = "version is required")
    @PositiveOrZero(message = "version must not be negative")
    private Long version;
    @NotNull(message = "occurredAt is required")
    private Instant occurredAt;
}
