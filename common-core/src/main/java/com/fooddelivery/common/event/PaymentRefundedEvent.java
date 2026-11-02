package com.fooddelivery.common.event;

import java.math.BigDecimal;
import java.util.UUID;
import com.fooddelivery.common.enums.PaymentGateway;
import com.fooddelivery.common.enums.RefundDestination;

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
public class PaymentRefundedEvent {
    private String orderId;
    private String gatewayOrderId;
    private BigDecimal amountRefunded;
    private PaymentGateway gatewayName;
    private RefundDestination refundDestination;
    private String refundId;
    private String gatewayRefundId;
    private String status;
    private Boolean isSuccess;
    private String failureReason;
    /**
     * When the refund moved money, stamped once by the system that completed it: PaymentGatewayIntegration for a gateway refund
     * (its {@code refunds.completed_at}), WalletService for store credit (the credit's wallet transaction). Required when
     * {@code isSuccess}: OrderService dates the refund, and its ledger posting, on it (ReconResidualsC7 Q3).
     */
    private java.time.Instant completedAt;
}
