package com.fooddelivery.common.constants;

public final class KafkaConstants {
    private KafkaConstants() {}
    
    public static final String TOPIC_ORDER_EVENTS = "order-events";
    public static final String TOPIC_ORDER_EVENTS_DLQ = "order-events-dlq";
    public static final String TOPIC_PAYMENT_EVENTS = "payment-events";
    public static final String TOPIC_PAYMENT_EVENTS_DLQ = "payment-events-dlq";
    public static final String TOPIC_NOTIFICATIONS_DISPATCH = "platform.notifications.dispatch";
    public static final String TOPIC_NOTIFICATIONS_DLQ = "notifications-dlq";
    public static final String TOPIC_LOGISTICS_DISPATCH = "platform.logistics.dispatch";
    public static final String TOPIC_RESTAURANT_EVENTS = "restaurant-events";
    public static final String GROUP_FOOD_DELIVERY = "food-delivery-group";
    public static final String GROUP_RESTAURANT_SERVICE = "restaurant-service-group";
    public static final String GROUP_DELIVERY_SERVICE = "delivery-service-group";
    public static final String GROUP_NOTIFICATION_SERVICE = "notification-service-group";
    public static final String GROUP_MAPS_INTEGRATION = "maps-integration-group";
    public static final String GROUP_GOV_ID_VALIDATION = "gov-id-validation-group";
    public static final String GROUP_PAYMENT_SERVICE = "payment-gateway-group";
}
