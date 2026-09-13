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
    public static final String TOPIC_MENU_EVENTS = "menu-events";
    public static final String TOPIC_LEDGER_EVENTS = "ledger-events";

    public static final String TOPIC_AD_EVENTS = "ad-events";
    public static final String TOPIC_AD_EVENTS_DLQ = "ad-events-dlq";
    public static final String TOPIC_AD_BILLING_EVENTS = "ad-billing-events";
    public static final String TOPIC_AD_BILLING_EVENTS_DLQ = "ad-billing-events-dlq";
    public static final String TOPIC_AD_TRACKING_EVENTS = "ad-tracking-events";
    public static final String TOPIC_WALLET_EVENTS = "wallet-events";
    public static final String TOPIC_WALLET_EVENTS_DLQ = "wallet-events-dlq";
    public static final String TOPIC_CHAT_EVENTS = "chat-events";
    public static final String TOPIC_CHAT_EVENTS_DLQ = "chat-events-dlq";
    public static final String TOPIC_REVIEW_EVENTS = "review-events";
    public static final String TOPIC_REVIEW_EVENTS_DLQ = "review-events-dlq";
    public static final String GROUP_FOOD_DELIVERY = "food-delivery-group";
    public static final String GROUP_RESTAURANT_SERVICE = "restaurant-service-group";
    public static final String GROUP_DELIVERY_SERVICE = "delivery-service-group";
    public static final String GROUP_NOTIFICATION_SERVICE = "notification-service-group";
    public static final String GROUP_MAPS_INTEGRATION = "maps-integration-group";
    public static final String GROUP_GOV_ID_VALIDATION = "gov-id-validation-group";
    public static final String GROUP_PAYMENT_SERVICE = "payment-gateway-group";
    public static final String GROUP_LEDGER_SERVICE = "ledger-service-group";
    public static final String GROUP_AD_SERVICE = "advertisement-service-group";
    
    // Tracing Headers
    public static final String HEADER_TRACE_ID = "X-B3-TraceId";
    public static final String HEADER_SPAN_ID = "X-B3-SpanId";
}
