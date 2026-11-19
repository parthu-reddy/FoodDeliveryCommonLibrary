package com.fooddelivery.common.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A guest's in-app payment towards a table bill was captured, failed or refunded (payment-events, aggregate PAYMENT,
 * key = paymentId; TableBill_2026-11-18 D6, D7). One class for TABLE_BILL_PAYMENT_COMPLETED, _FAILED and _REFUNDED: the
 * event type travels in the header, as on every outbox event. Modelled on {@link ReservationPaymentEvent}.
 *
 * <p>Producer: PaymentGatewayIntegration, choosing the event type by the intent's purpose {@code TABLE_BILL}. Consumer:
 * order-service, which owns the bill. {@code paymentId} is the {@code table_bill_payments.id} that was the create-order
 * reference (D7), so the consumer resolves the bill from its own payment row. {@code occurredAt} is when the money moved, on
 * the gateway's clock. {@code refundId} is set on REFUNDED, and on a FAILED that reports a refund the provider refused;
 * {@code failureReason} is set on FAILED only. Unset optional fields are left off the wire. Ignores unknown fields because
 * payment bodies repeat {@code eventType} (see {@link PaymentSucceededEvent}).
 */
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class TableBillPaymentEvent {
    @NotNull(message = "paymentId is required")
    private UUID paymentId;
    @NotBlank(message = "gatewayOrderId is required")
    private String gatewayOrderId;
    /** Captured, attempted or refunded, by event type. */
    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;
    private String gatewayName;
    private String refundId;
    private String failureReason;
    @NotNull(message = "occurredAt is required")
    private Instant occurredAt;
}
