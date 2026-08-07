package com.fooddelivery.common.event;

import java.util.UUID;

public class OrderPaidEvent {
    private UUID orderId;
    private UUID restaurantId;
    private String customerName;
    private Integer estimatedPrepTimeMinutes;
    private Double deliveryLat;
    private Double deliveryLng;
    private String deliveryAddress;
    private String itemsJson;
    private String pickupOtp;
    private String deliveryOtp;


    @java.lang.SuppressWarnings("all")
    public static class OrderPaidEventBuilder {
        @java.lang.SuppressWarnings("all")
        private UUID orderId;
        @java.lang.SuppressWarnings("all")
        private UUID restaurantId;
        @java.lang.SuppressWarnings("all")
        private String customerName;
        @java.lang.SuppressWarnings("all")
        private Integer estimatedPrepTimeMinutes;
        @java.lang.SuppressWarnings("all")
        private Double deliveryLat;
        @java.lang.SuppressWarnings("all")
        private Double deliveryLng;
        @java.lang.SuppressWarnings("all")
        private String deliveryAddress;
        @java.lang.SuppressWarnings("all")
        private String itemsJson;
        @java.lang.SuppressWarnings("all")
        private String pickupOtp;
        @java.lang.SuppressWarnings("all")
        private String deliveryOtp;

        @java.lang.SuppressWarnings("all")
        OrderPaidEventBuilder() {
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderPaidEvent.OrderPaidEventBuilder orderId(final UUID orderId) {
            this.orderId = orderId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderPaidEvent.OrderPaidEventBuilder restaurantId(final UUID restaurantId) {
            this.restaurantId = restaurantId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderPaidEvent.OrderPaidEventBuilder customerName(final String customerName) {
            this.customerName = customerName;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderPaidEvent.OrderPaidEventBuilder estimatedPrepTimeMinutes(final Integer estimatedPrepTimeMinutes) {
            this.estimatedPrepTimeMinutes = estimatedPrepTimeMinutes;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderPaidEvent.OrderPaidEventBuilder deliveryLat(final Double deliveryLat) {
            this.deliveryLat = deliveryLat;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderPaidEvent.OrderPaidEventBuilder deliveryLng(final Double deliveryLng) {
            this.deliveryLng = deliveryLng;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderPaidEvent.OrderPaidEventBuilder deliveryAddress(final String deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderPaidEvent.OrderPaidEventBuilder itemsJson(final String itemsJson) {
            this.itemsJson = itemsJson;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderPaidEvent.OrderPaidEventBuilder pickupOtp(final String pickupOtp) {
            this.pickupOtp = pickupOtp;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public OrderPaidEvent.OrderPaidEventBuilder deliveryOtp(final String deliveryOtp) {
            this.deliveryOtp = deliveryOtp;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        public OrderPaidEvent build() {
            return new OrderPaidEvent(this.orderId, this.restaurantId, this.customerName, this.estimatedPrepTimeMinutes, this.deliveryLat, this.deliveryLng, this.deliveryAddress, this.itemsJson, this.pickupOtp, this.deliveryOtp);
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        public java.lang.String toString() {
            return "OrderPaidEvent.OrderPaidEventBuilder(orderId=" + this.orderId + ", restaurantId=" + this.restaurantId + ", customerName=" + this.customerName + ", estimatedPrepTimeMinutes=" + this.estimatedPrepTimeMinutes + ", deliveryLat=" + this.deliveryLat + ", deliveryLng=" + this.deliveryLng + ", deliveryAddress=" + this.deliveryAddress + ", itemsJson=" + this.itemsJson + ", pickupOtp=" + this.pickupOtp + ", deliveryOtp=" + this.deliveryOtp + ")";
        }
    }

    @java.lang.SuppressWarnings("all")
    public static OrderPaidEvent.OrderPaidEventBuilder builder() {
        return new OrderPaidEvent.OrderPaidEventBuilder();
    }

    @java.lang.SuppressWarnings("all")
    public UUID getOrderId() {
        return this.orderId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getRestaurantId() {
        return this.restaurantId;
    }

    @java.lang.SuppressWarnings("all")
    public String getCustomerName() {
        return this.customerName;
    }

    @java.lang.SuppressWarnings("all")
    public Integer getEstimatedPrepTimeMinutes() {
        return this.estimatedPrepTimeMinutes;
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
    public String getItemsJson() {
        return this.itemsJson;
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
    public void setRestaurantId(final UUID restaurantId) {
        this.restaurantId = restaurantId;
    }

    @java.lang.SuppressWarnings("all")
    public void setCustomerName(final String customerName) {
        this.customerName = customerName;
    }

    @java.lang.SuppressWarnings("all")
    public void setEstimatedPrepTimeMinutes(final Integer estimatedPrepTimeMinutes) {
        this.estimatedPrepTimeMinutes = estimatedPrepTimeMinutes;
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
    public void setItemsJson(final String itemsJson) {
        this.itemsJson = itemsJson;
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
        if (!(o instanceof OrderPaidEvent)) return false;
        final OrderPaidEvent other = (OrderPaidEvent) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        final java.lang.Object this$estimatedPrepTimeMinutes = this.getEstimatedPrepTimeMinutes();
        final java.lang.Object other$estimatedPrepTimeMinutes = other.getEstimatedPrepTimeMinutes();
        if (this$estimatedPrepTimeMinutes == null ? other$estimatedPrepTimeMinutes != null : !this$estimatedPrepTimeMinutes.equals(other$estimatedPrepTimeMinutes)) return false;
        final java.lang.Object this$deliveryLat = this.getDeliveryLat();
        final java.lang.Object other$deliveryLat = other.getDeliveryLat();
        if (this$deliveryLat == null ? other$deliveryLat != null : !this$deliveryLat.equals(other$deliveryLat)) return false;
        final java.lang.Object this$deliveryLng = this.getDeliveryLng();
        final java.lang.Object other$deliveryLng = other.getDeliveryLng();
        if (this$deliveryLng == null ? other$deliveryLng != null : !this$deliveryLng.equals(other$deliveryLng)) return false;
        final java.lang.Object this$orderId = this.getOrderId();
        final java.lang.Object other$orderId = other.getOrderId();
        if (this$orderId == null ? other$orderId != null : !this$orderId.equals(other$orderId)) return false;
        final java.lang.Object this$restaurantId = this.getRestaurantId();
        final java.lang.Object other$restaurantId = other.getRestaurantId();
        if (this$restaurantId == null ? other$restaurantId != null : !this$restaurantId.equals(other$restaurantId)) return false;
        final java.lang.Object this$customerName = this.getCustomerName();
        final java.lang.Object other$customerName = other.getCustomerName();
        if (this$customerName == null ? other$customerName != null : !this$customerName.equals(other$customerName)) return false;
        final java.lang.Object this$deliveryAddress = this.getDeliveryAddress();
        final java.lang.Object other$deliveryAddress = other.getDeliveryAddress();
        if (this$deliveryAddress == null ? other$deliveryAddress != null : !this$deliveryAddress.equals(other$deliveryAddress)) return false;
        final java.lang.Object this$itemsJson = this.getItemsJson();
        final java.lang.Object other$itemsJson = other.getItemsJson();
        if (this$itemsJson == null ? other$itemsJson != null : !this$itemsJson.equals(other$itemsJson)) return false;
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
        return other instanceof OrderPaidEvent;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $estimatedPrepTimeMinutes = this.getEstimatedPrepTimeMinutes();
        result = result * PRIME + ($estimatedPrepTimeMinutes == null ? 43 : $estimatedPrepTimeMinutes.hashCode());
        final java.lang.Object $deliveryLat = this.getDeliveryLat();
        result = result * PRIME + ($deliveryLat == null ? 43 : $deliveryLat.hashCode());
        final java.lang.Object $deliveryLng = this.getDeliveryLng();
        result = result * PRIME + ($deliveryLng == null ? 43 : $deliveryLng.hashCode());
        final java.lang.Object $orderId = this.getOrderId();
        result = result * PRIME + ($orderId == null ? 43 : $orderId.hashCode());
        final java.lang.Object $restaurantId = this.getRestaurantId();
        result = result * PRIME + ($restaurantId == null ? 43 : $restaurantId.hashCode());
        final java.lang.Object $customerName = this.getCustomerName();
        result = result * PRIME + ($customerName == null ? 43 : $customerName.hashCode());
        final java.lang.Object $deliveryAddress = this.getDeliveryAddress();
        result = result * PRIME + ($deliveryAddress == null ? 43 : $deliveryAddress.hashCode());
        final java.lang.Object $itemsJson = this.getItemsJson();
        result = result * PRIME + ($itemsJson == null ? 43 : $itemsJson.hashCode());
        final java.lang.Object $pickupOtp = this.getPickupOtp();
        result = result * PRIME + ($pickupOtp == null ? 43 : $pickupOtp.hashCode());
        final java.lang.Object $deliveryOtp = this.getDeliveryOtp();
        result = result * PRIME + ($deliveryOtp == null ? 43 : $deliveryOtp.hashCode());
        return result;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "OrderPaidEvent(orderId=" + this.getOrderId() + ", restaurantId=" + this.getRestaurantId() + ", customerName=" + this.getCustomerName() + ", estimatedPrepTimeMinutes=" + this.getEstimatedPrepTimeMinutes() + ", deliveryLat=" + this.getDeliveryLat() + ", deliveryLng=" + this.getDeliveryLng() + ", deliveryAddress=" + this.getDeliveryAddress() + ", itemsJson=" + this.getItemsJson() + ", pickupOtp=" + this.getPickupOtp() + ", deliveryOtp=" + this.getDeliveryOtp() + ")";
    }

    @java.lang.SuppressWarnings("all")
    public OrderPaidEvent() {
    }

    @java.lang.SuppressWarnings("all")
    public OrderPaidEvent(final UUID orderId, final UUID restaurantId, final String customerName, final Integer estimatedPrepTimeMinutes, final Double deliveryLat, final Double deliveryLng, final String deliveryAddress, final String itemsJson, final String pickupOtp, final String deliveryOtp) {
        this.orderId = orderId;
        this.restaurantId = restaurantId;
        this.customerName = customerName;
        this.estimatedPrepTimeMinutes = estimatedPrepTimeMinutes;
        this.deliveryLat = deliveryLat;
        this.deliveryLng = deliveryLng;
        this.deliveryAddress = deliveryAddress;
        this.itemsJson = itemsJson;
        this.pickupOtp = pickupOtp;
        this.deliveryOtp = deliveryOtp;
    }
}
