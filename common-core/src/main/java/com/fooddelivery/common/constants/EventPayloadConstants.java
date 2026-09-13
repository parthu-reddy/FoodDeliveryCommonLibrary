package com.fooddelivery.common.constants;

public final class EventPayloadConstants {
    
    private EventPayloadConstants() {
        // Prevent instantiation
    }
    
    public static final String EVENT_TYPE = "eventType";
    public static final String PAYLOAD = "payload";
    public static final String AGGREGATE_TYPE = "aggregateType";
    
    // Inner Payload Keys
    public static final String EVENT_ID = "eventId";
    public static final String ADVERTISER_ID = "advertiserId";
    public static final String CAMPAIGN_ID = "campaignId";
    public static final String AMOUNT = "amount";
    public static final String CHARGE_CATEGORY = "chargeCategory";
    public static final String TIMESTAMP = "timestamp";
    public static final String ORDER_ID = "orderId";
    public static final String DEVICE_ID = "deviceId";
    public static final String TRANSACTION_ID = "transactionId";
    public static final String STATUS = "status";
    public static final String DEBIT_ACCOUNT_ID = "debitAccountId";

    // REVIEW_CREATED payload, on review-events.
    //
    // Shared deliberately: ReviewCommandService writes these keys and RestaurantApplication's
    // ReviewEventConsumer reads them, in two modules that never see each other's source. Referencing
    // one constant makes a rename a compile error on both sides at once -- a stronger guarantee than
    // a contract test, which would catch the same drift only when it ran.
    public static final String REVIEW_ID = "reviewId";
    public static final String ENTITY_TYPE = "entityType";
    public static final String ENTITY_ID = "entityId";
    public static final String USER_ID = "userId";
    public static final String RATING = "rating";
    /** The aggregate's state AFTER the write, not a delta -- consumers assign it verbatim. */
    public static final String TOTAL_REVIEWS = "totalReviews";
    /** Likewise absolute. Serialised as a plain string to avoid a double round-trip. */
    public static final String AVERAGE_RATING = "averageRating";
}
