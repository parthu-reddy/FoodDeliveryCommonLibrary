package com.fooddelivery.common.event;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderCreatedEvent {
    private UUID orderId;
    private UUID customerId;
    private UUID restaurantId;
    private BigDecimal totalAmount;
    private Double deliveryLat;
    private Double deliveryLng;
    private String deliveryAddress;
    private String pickupOtp;
    private String deliveryOtp;


    @java.lang.SuppressWarnings("all")
    public static class OrderCreatedEventBuilder {
        @java.lang.SuppressWarnings("all")
        private UUID orderId;
        @java.lang.SuppressWarnings("all")
        private UUID customerId;
        @java.lang.SuppressWarnings("all")
        private UUID restaurantId;
        @java.lang.SuppressWarnings("all")
        private BigDecimal totalAmount;
        @java.lang.SuppressWarnings("all")
        private Double deliveryLat;
        @java.lang.SuppressWarnings("all")
        private Double deliveryLng;
        @java.lang.SuppressWarnings("all")
        private String deliveryAddress;
        @java.lang.SuppressWarnings("all")
        private String pickupOtp;
        @java.lang.SuppressWarnings("all")
        private String deliveryOtp;

        @java.lang.SuppressWarnings("all")
        OrderCreatedEventBuilder() {
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderCreatedEvent.OrderCreatedEventBuilder orderId(final UUID orderId) {
            this.orderId = orderId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderCreatedEvent.OrderCreatedEventBuilder customerId(final UUID customerId) {
            this.customerId = customerId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderCreatedEvent.OrderCreatedEventBuilder restaurantId(final UUID restaurantId) {
            this.restaurantId = restaurantId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderCreatedEvent.OrderCreatedEventBuilder totalAmount(final BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderCreatedEvent.OrderCreatedEventBuilder deliveryLat(final Double deliveryLat) {
            this.deliveryLat = deliveryLat;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderCreatedEvent.OrderCreatedEventBuilder deliveryLng(final Double deliveryLng) {
            this.deliveryLng = deliveryLng;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderCreatedEvent.OrderCreatedEventBuilder deliveryAddress(final String deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderCreatedEvent.OrderCreatedEventBuilder pickupOtp(final String pickupOtp) {
            this.pickupOtp = pickupOtp;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderCreatedEvent.OrderCreatedEventBuilder deliveryOtp(final String deliveryOtp) {
            this.deliveryOtp = deliveryOtp;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        public OrderCreatedEvent build() {
            return new OrderCreatedEvent(this.orderId, this.customerId, this.restaurantId, this.totalAmount, this.deliveryLat, this.deliveryLng, this.deliveryAddress, this.pickupOtp, this.deliveryOtp);
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        public java.lang.String toString() {
            return "OrderCreatedEvent.OrderCreatedEventBuilder(orderId=" + this.orderId + ", customerId=" + this.customerId + ", restaurantId=" + this.restaurantId + ", totalAmount=" + this.totalAmount + ", deliveryLat=" + this.deliveryLat + ", deliveryLng=" + this.deliveryLng + ", deliveryAddress=" + this.deliveryAddress + ", pickupOtp=" + this.pickupOtp + ", deliveryOtp=" + this.deliveryOtp + ")";
        }
    }

    @java.lang.SuppressWarnings("all")
    public static OrderCreatedEvent.OrderCreatedEventBuilder builder() {
        return new OrderCreatedEvent.OrderCreatedEventBuilder();
    }

    @java.lang.SuppressWarnings("all")
    public UUID getOrderId() {
        return this.orderId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getCustomerId() {
        return this.customerId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getRestaurantId() {
        return this.restaurantId;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getTotalAmount() {
        return this.totalAmount;
    }

    @java.lang.SuppressWarnings("all")
    public Double getDeliveryLat() {
        return this.deliveryLat;
    }

    @java.lang.SuppressWarnings("all")
    public Double getDeliveryLng() {
        return this.deliveryLng;
    }

    @java.lang.SuppressWarnings("all")
    public String getDeliveryAddress() {
        return this.deliveryAddress;
    }

    @java.lang.SuppressWarnings("all")
    public String getPickupOtp() {
        return this.pickupOtp;
    }

    @java.lang.SuppressWarnings("all")
    public String getDeliveryOtp() {
        return this.deliveryOtp;
    }

    @java.lang.SuppressWarnings("all")
    public void setOrderId(final UUID orderId) {
        this.orderId = orderId;
    }

    @java.lang.SuppressWarnings("all")
    public void setCustomerId(final UUID customerId) {
        this.customerId = customerId;
    }

    @java.lang.SuppressWarnings("all")
    public void setRestaurantId(final UUID restaurantId) {
        this.restaurantId = restaurantId;
    }

    @java.lang.SuppressWarnings("all")
    public void setTotalAmount(final BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    @java.lang.SuppressWarnings("all")
    public void setDeliveryLat(final Double deliveryLat) {
        this.deliveryLat = deliveryLat;
    }

    @java.lang.SuppressWarnings("all")
    public void setDeliveryLng(final Double deliveryLng) {
        this.deliveryLng = deliveryLng;
    }

    @java.lang.SuppressWarnings("all")
    public void setDeliveryAddress(final String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    @java.lang.SuppressWarnings("all")
    public void setPickupOtp(final String pickupOtp) {
        this.pickupOtp = pickupOtp;
    }

    @java.lang.SuppressWarnings("all")
    public void setDeliveryOtp(final String deliveryOtp) {
        this.deliveryOtp = deliveryOtp;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof OrderCreatedEvent)) return false;
        final OrderCreatedEvent other = (OrderCreatedEvent) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        final java.lang.Object this$deliveryLat = this.getDeliveryLat();
        final java.lang.Object other$deliveryLat = other.getDeliveryLat();
        if (this$deliveryLat == null ? other$deliveryLat != null : !this$deliveryLat.equals(other$deliveryLat)) return false;
        final java.lang.Object this$deliveryLng = this.getDeliveryLng();
        final java.lang.Object other$deliveryLng = other.getDeliveryLng();
        if (this$deliveryLng == null ? other$deliveryLng != null : !this$deliveryLng.equals(other$deliveryLng)) return false;
        final java.lang.Object this$orderId = this.getOrderId();
        final java.lang.Object other$orderId = other.getOrderId();
        if (this$orderId == null ? other$orderId != null : !this$orderId.equals(other$orderId)) return false;
        final java.lang.Object this$customerId = this.getCustomerId();
        final java.lang.Object other$customerId = other.getCustomerId();
        if (this$customerId == null ? other$customerId != null : !this$customerId.equals(other$customerId)) return false;
        final java.lang.Object this$restaurantId = this.getRestaurantId();
        final java.lang.Object other$restaurantId = other.getRestaurantId();
        if (this$restaurantId == null ? other$restaurantId != null : !this$restaurantId.equals(other$restaurantId)) return false;
        final java.lang.Object this$totalAmount = this.getTotalAmount();
        final java.lang.Object other$totalAmount = other.getTotalAmount();
        if (this$totalAmount == null ? other$totalAmount != null : !this$totalAmount.equals(other$totalAmount)) return false;
        final java.lang.Object this$deliveryAddress = this.getDeliveryAddress();
        final java.lang.Object other$deliveryAddress = other.getDeliveryAddress();
        if (this$deliveryAddress == null ? other$deliveryAddress != null : !this$deliveryAddress.equals(other$deliveryAddress)) return false;
        final java.lang.Object this$pickupOtp = this.getPickupOtp();
        final java.lang.Object other$pickupOtp = other.getPickupOtp();
        if (this$pickupOtp == null ? other$pickupOtp != null : !this$pickupOtp.equals(other$pickupOtp)) return false;
        final java.lang.Object this$deliveryOtp = this.getDeliveryOtp();
        final java.lang.Object other$deliveryOtp = other.getDeliveryOtp();
        if (this$deliveryOtp == null ? other$deliveryOtp != null : !this$deliveryOtp.equals(other$deliveryOtp)) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof OrderCreatedEvent;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $deliveryLat = this.getDeliveryLat();
        result = result * PRIME + ($deliveryLat == null ? 43 : $deliveryLat.hashCode());
        final java.lang.Object $deliveryLng = this.getDeliveryLng();
        result = result * PRIME + ($deliveryLng == null ? 43 : $deliveryLng.hashCode());
        final java.lang.Object $orderId = this.getOrderId();
        result = result * PRIME + ($orderId == null ? 43 : $orderId.hashCode());
        final java.lang.Object $customerId = this.getCustomerId();
        result = result * PRIME + ($customerId == null ? 43 : $customerId.hashCode());
        final java.lang.Object $restaurantId = this.getRestaurantId();
        result = result * PRIME + ($restaurantId == null ? 43 : $restaurantId.hashCode());
        final java.lang.Object $totalAmount = this.getTotalAmount();
        result = result * PRIME + ($totalAmount == null ? 43 : $totalAmount.hashCode());
        final java.lang.Object $deliveryAddress = this.getDeliveryAddress();
        result = result * PRIME + ($deliveryAddress == null ? 43 : $deliveryAddress.hashCode());
        final java.lang.Object $pickupOtp = this.getPickupOtp();
        result = result * PRIME + ($pickupOtp == null ? 43 : $pickupOtp.hashCode());
        final java.lang.Object $deliveryOtp = this.getDeliveryOtp();
        result = result * PRIME + ($deliveryOtp == null ? 43 : $deliveryOtp.hashCode());
        return result;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "OrderCreatedEvent(orderId=" + this.getOrderId() + ", customerId=" + this.getCustomerId() + ", restaurantId=" + this.getRestaurantId() + ", totalAmount=" + this.getTotalAmount() + ", deliveryLat=" + this.getDeliveryLat() + ", deliveryLng=" + this.getDeliveryLng() + ", deliveryAddress=" + this.getDeliveryAddress() + ", pickupOtp=" + this.getPickupOtp() + ", deliveryOtp=" + this.getDeliveryOtp() + ")";
    }

    @java.lang.SuppressWarnings("all")
    public OrderCreatedEvent() {
    }

    @java.lang.SuppressWarnings("all")
    public OrderCreatedEvent(final UUID orderId, final UUID customerId, final UUID restaurantId, final BigDecimal totalAmount, final Double deliveryLat, final Double deliveryLng, final String deliveryAddress, final String pickupOtp, final String deliveryOtp) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.totalAmount = totalAmount;
        this.deliveryLat = deliveryLat;
        this.deliveryLng = deliveryLng;
        this.deliveryAddress = deliveryAddress;
        this.pickupOtp = pickupOtp;
        this.deliveryOtp = deliveryOtp;
    }
}
