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
}
