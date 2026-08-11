package com.fooddelivery.common.event;

import java.math.BigDecimal;
import java.util.UUID;
import com.fooddelivery.common.enums.PaymentGateway;
import com.fooddelivery.common.enums.RefundDestination;

public class PaymentRefundedEvent {
    private String orderId;
    private String gatewayOrderId;
    private BigDecimal amountRefunded;
    private PaymentGateway gatewayName;
    private RefundDestination refundDestination;


    @java.lang.SuppressWarnings("all")
    public static class PaymentRefundedEventBuilder {
        @java.lang.SuppressWarnings("all")
        private String orderId;
        @java.lang.SuppressWarnings("all")
        private String gatewayOrderId;
        @java.lang.SuppressWarnings("all")
        private BigDecimal amountRefunded;
        @java.lang.SuppressWarnings("all")
        private PaymentGateway gatewayName;
        @java.lang.SuppressWarnings("all")
        private RefundDestination refundDestination;

        @java.lang.SuppressWarnings("all")
        PaymentRefundedEventBuilder() {
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public PaymentRefundedEvent.PaymentRefundedEventBuilder orderId(final String orderId) {
            this.orderId = orderId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public PaymentRefundedEvent.PaymentRefundedEventBuilder gatewayOrderId(final String gatewayOrderId) {
            this.gatewayOrderId = gatewayOrderId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public PaymentRefundedEvent.PaymentRefundedEventBuilder amountRefunded(final BigDecimal amountRefunded) {
            this.amountRefunded = amountRefunded;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public PaymentRefundedEvent.PaymentRefundedEventBuilder gatewayName(final PaymentGateway gatewayName) {
            this.gatewayName = gatewayName;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public PaymentRefundedEvent.PaymentRefundedEventBuilder refundDestination(final RefundDestination refundDestination) {
            this.refundDestination = refundDestination;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        public PaymentRefundedEvent build() {
            return new PaymentRefundedEvent(this.orderId, this.gatewayOrderId, this.amountRefunded, this.gatewayName, this.refundDestination);
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        public java.lang.String toString() {
            return "PaymentRefundedEvent.PaymentRefundedEventBuilder(orderId=" + this.orderId + ", gatewayOrderId=" + this.gatewayOrderId + ", amountRefunded=" + this.amountRefunded + ", gatewayName=" + this.gatewayName + ", refundDestination=" + this.refundDestination + ")";
        }
    }

    @java.lang.SuppressWarnings("all")
    public static PaymentRefundedEvent.PaymentRefundedEventBuilder builder() {
        return new PaymentRefundedEvent.PaymentRefundedEventBuilder();
    }

    @java.lang.SuppressWarnings("all")
    public String getOrderId() {
        return this.orderId;
    }

    @java.lang.SuppressWarnings("all")
    public String getGatewayOrderId() {
        return this.gatewayOrderId;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getAmountRefunded() {
        return this.amountRefunded;
    }

    @java.lang.SuppressWarnings("all")
    public PaymentGateway getGatewayName() {
        return this.gatewayName;
    }

    @java.lang.SuppressWarnings("all")
    public RefundDestination getRefundDestination() {
        return this.refundDestination;
    }

    @java.lang.SuppressWarnings("all")
    public void setOrderId(final String orderId) {
        this.orderId = orderId;
    }

    @java.lang.SuppressWarnings("all")
    public void setGatewayOrderId(final String gatewayOrderId) {
        this.gatewayOrderId = gatewayOrderId;
    }

    @java.lang.SuppressWarnings("all")
    public void setAmountRefunded(final BigDecimal amountRefunded) {
        this.amountRefunded = amountRefunded;
    }

    @java.lang.SuppressWarnings("all")
    public void setGatewayName(final PaymentGateway gatewayName) {
        this.gatewayName = gatewayName;
    }

    @java.lang.SuppressWarnings("all")
    public void setRefundDestination(final RefundDestination refundDestination) {
        this.refundDestination = refundDestination;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PaymentRefundedEvent)) return false;
        final PaymentRefundedEvent other = (PaymentRefundedEvent) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        final java.lang.Object this$orderId = this.getOrderId();
        final java.lang.Object other$orderId = other.getOrderId();
        if (this$orderId == null ? other$orderId != null : !this$orderId.equals(other$orderId)) return false;
        final java.lang.Object this$gatewayOrderId = this.getGatewayOrderId();
        final java.lang.Object other$gatewayOrderId = other.getGatewayOrderId();
        if (this$gatewayOrderId == null ? other$gatewayOrderId != null : !this$gatewayOrderId.equals(other$gatewayOrderId)) return false;
        final java.lang.Object this$amountRefunded = this.getAmountRefunded();
        final java.lang.Object other$amountRefunded = other.getAmountRefunded();
        if (this$amountRefunded == null ? other$amountRefunded != null : !this$amountRefunded.equals(other$amountRefunded)) return false;
        final java.lang.Object this$gatewayName = this.getGatewayName();
        final java.lang.Object other$gatewayName = other.getGatewayName();
        if (this$gatewayName == null ? other$gatewayName != null : !this$gatewayName.equals(other$gatewayName)) return false;
        final java.lang.Object this$refundDestination = this.getRefundDestination();
        final java.lang.Object other$refundDestination = other.getRefundDestination();
        if (this$refundDestination == null ? other$refundDestination != null : !this$refundDestination.equals(other$refundDestination)) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof PaymentRefundedEvent;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $orderId = this.getOrderId();
        result = result * PRIME + ($orderId == null ? 43 : $orderId.hashCode());
        final java.lang.Object $gatewayOrderId = this.getGatewayOrderId();
        result = result * PRIME + ($gatewayOrderId == null ? 43 : $gatewayOrderId.hashCode());
        final java.lang.Object $amountRefunded = this.getAmountRefunded();
        result = result * PRIME + ($amountRefunded == null ? 43 : $amountRefunded.hashCode());
        final java.lang.Object $gatewayName = this.getGatewayName();
        result = result * PRIME + ($gatewayName == null ? 43 : $gatewayName.hashCode());
        final java.lang.Object $refundDestination = this.getRefundDestination();
        result = result * PRIME + ($refundDestination == null ? 43 : $refundDestination.hashCode());
        return result;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "PaymentRefundedEvent(orderId=" + this.getOrderId() + ", gatewayOrderId=" + this.getGatewayOrderId() + ", amountRefunded=" + this.getAmountRefunded() + ", gatewayName=" + this.getGatewayName() + ", refundDestination=" + this.getRefundDestination() + ")";
    }

    @java.lang.SuppressWarnings("all")
    public PaymentRefundedEvent() {
    }

    @java.lang.SuppressWarnings("all")
    public PaymentRefundedEvent(final String orderId, final String gatewayOrderId, final BigDecimal amountRefunded, final PaymentGateway gatewayName, final RefundDestination refundDestination) {
        this.orderId = orderId;
        this.gatewayOrderId = gatewayOrderId;
        this.amountRefunded = amountRefunded;
        this.gatewayName = gatewayName;
        this.refundDestination = refundDestination;
    }
}
