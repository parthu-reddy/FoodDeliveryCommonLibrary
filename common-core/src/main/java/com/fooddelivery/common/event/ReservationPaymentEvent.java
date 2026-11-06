package com.fooddelivery.common.event;

import com.fooddelivery.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A booking deposit's payment was captured, failed or refunded (payment-events, aggregate PAYMENT, key = referenceId;
 * DiningPickupBooking D49). One class for RESERVATION_PAYMENT_COMPLETED, _FAILED and _REFUNDED: the event type travels
 * in the header, as on every outbox event.
 *
 * <p>Producer: PaymentGatewayIntegration, choosing the event type by the intent's payment purpose (Phase 4).
 * Consumers: reservation-service's deposit service (Phase 6). order-service never receives one, so a deposit can never
 * be mistaken for an order payment. {@code occurredAt} is when the money moved, on the gateway's clock: the ledger dates
 * the posting on it (D50). {@code referenceId} is the create-order reference, which the gateway treats as opaque: the
 * consumer resolves the reservation from its own deposit payment row. {@code refundId} is set on REFUNDED, and on a FAILED
 * that reports a refund the provider refused, which is how that is told from a refused capture; {@code failureReason} is set
 * on FAILED only. Ignores unknown fields because payment bodies repeat {@code eventType} (see {@link PaymentSucceededEvent}).
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ReservationPaymentEvent {
    @NotBlank(message = "referenceId is required")
    private String referenceId;
    @NotBlank(message = "gatewayOrderId is required")
    private String gatewayOrderId;
    /** Captured, attempted or refunded, by event type. */
    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;
    private String gatewayName;
    private PaymentMethod paymentMethod;
    private String refundId;
    private String failureReason;
    @NotNull(message = "occurredAt is required")
    private Instant occurredAt;
}
