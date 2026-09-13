package com.fooddelivery.common.constants;

public final class AppConstants {
    private AppConstants() {}
    
    /**
     * How far a customer may be from a restaurant, in kilometres. The quote is rejected past it.
     *
     * <p>Distinct from {@link #FLEET_SEARCH_RADIUS_KM} because they are different questions.
     * `CustomerOrderService` used to reject a quote over a hardcoded 7.0 while the fleet check used
     * this 5.0, so a restaurant 6 km away quoted successfully and then failed at checkout with
     * "All our delivery partners are currently busy" -- which was not what had happened.
     *
     * <p>These are the defaults. `DeliveryZoneConfig` binds the live values.
     */
    public static final double MAX_DELIVERY_RADIUS_KM = 5.0;
    /** How far from the restaurant to look for a rider, in kilometres. */
    public static final double FLEET_SEARCH_RADIUS_KM = 5.0;
    public static final String MAX_DELIVERY_RADIUS_KM_STR = "5.0";
    public static final String MAX_DELIVERY_RADIUS_METERS_STR = "5000";

    // Error messages and codes
    public static final String ERROR_NO_DELIVERY_PARTNER_NEARBY = "NO_DELIVERY_PARTNER_NEARBY";
    public static final String ERROR_MSG_NO_DELIVERY_PARTNER_NEARBY = "No delivery partner near that restaurant, please look for another restaurant.";
    public static final String ERROR_MSG_RESTAURANT_UNKNOWN = "Restaurant location is unknown, cannot check delivery availability.";
    public static final String ERROR_MSG_RESTAURANT_NOT_FOUND = "Restaurant not found: ";



    // Outbox aggregate types


    // Retry limits
    public static final int MAX_OPTIMISTIC_LOCK_RETRIES = 5;

    // Allowed Origins for WebSocket and CORS
    public static final String[] ALLOWED_ORIGINS = {
            "https://driver.fooddelivery.com",
            "https://admin.fooddelivery.com",
            "https://user.fooddelivery.com",
            "https://restaurant.fooddelivery.com"
    };
}
