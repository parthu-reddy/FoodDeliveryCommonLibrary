package com.fooddelivery.common.constants;

public final class AppConstants {
    private AppConstants() {}
    
    // The maximum radius in kilometers for delivery and searching for available partners
    public static final double MAX_DELIVERY_RADIUS_KM = 5.0;
    public static final String MAX_DELIVERY_RADIUS_KM_STR = "5.0";
    public static final String MAX_DELIVERY_RADIUS_METERS_STR = "5000";

    // Error messages and codes
    public static final String ERROR_NO_DELIVERY_PARTNER_NEARBY = "NO_DELIVERY_PARTNER_NEARBY";
    public static final String ERROR_MSG_NO_DELIVERY_PARTNER_NEARBY = "No delivery partner near that restaurant, please look for another restaurant.";
    public static final String ERROR_MSG_RESTAURANT_UNKNOWN = "Restaurant location is unknown, cannot check delivery availability.";
    public static final String ERROR_MSG_RESTAURANT_NOT_FOUND = "Restaurant not found: ";

    // Default city identifier (single-city MVP)
    public static final String DEFAULT_CITY_ID = "BLR";

    // Outbox aggregate types
    public static final String AGGREGATE_ORDER = "Order";
    public static final String AGGREGATE_PAYMENT = "Payment";
    public static final String AGGREGATE_NOTIFICATION = "Notification";

    // Outbox event statuses
    public static final String OUTBOX_STATUS_UNPROCESSED = "UNPROCESSED";
    public static final String OUTBOX_STATUS_PROCESSED = "PROCESSED";
    public static final String OUTBOX_STATUS_FAILED = "FAILED";
    public static final String OUTBOX_STATUS_DLQ = "DLQ";

    // Ledger account types
    public static final String ACCOUNT_TYPE_CUSTOMER = "CUSTOMER";
    public static final String ACCOUNT_TYPE_PLATFORM = "PLATFORM";
    public static final String ACCOUNT_TYPE_RESTAURANT = "RESTAURANT";
    public static final String ACCOUNT_TYPE_DRIVER = "DRIVER";

    // Retry limits
    public static final int MAX_OPTIMISTIC_LOCK_RETRIES = 5;
}
