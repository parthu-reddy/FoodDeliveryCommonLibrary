package com.fooddelivery.common.constants;

public final class RedisKeyConstants {
    private RedisKeyConstants() {}

    public static final String PREFIX_ORDER_DRIVER_LOCK = "order:driver:lock:";
    public static final String PREFIX_DRIVER_PENDING_PING = "driver:pending_ping:";
    public static final String PREFIX_ORDER_RESTAURANT_STATUS = "order:restaurantStatus:";
    public static final String PREFIX_ORDER_DISPATCH_LOCK = "order:dispatch:lock:";
    public static final String PREFIX_ORDER_DISPATCH_PAYLOAD = "order:dispatchPayload:";
    public static final String PREFIX_DRIVER_ACTIVE_ORDER = "driver:active_order:";
    
    public static final String PREFIX_ORDER_PING_PENDING = "order:ping:pending:";
    public static final String PREFIX_ORDER_PING_TIMEOUTS = "order:ping:timeouts";
    public static final String PREFIX_ORDER_REJECTED_DRIVERS = "order:rejected_drivers:";
    
    public static final String LOCK_POLL_DELAYED_DISPATCHES = "lock:pollDelayedDispatches";
    public static final String LOCK_POLL_PING_TIMEOUTS = "lock:pollPingTimeouts";
    public static final String LOCK_SWEEP_STALE_DRIVERS = "lock:sweepStaleDrivers";
    public static final String LOCK_SWEEP_ABANDONED_DELIVERIES = "lock:sweepAbandonedDeliveries";
    public static final String LOCK_SWEEP_REFUND_RETRIES = "lock:sweepRefundRetries";
    public static final String LOCK_SWEEP_STALE_CREATED_ORDERS = "lock:sweepStaleCreatedOrders";
    public static final String LOCK_SWEEP_RESTAURANT_TIMEOUTS = "lock:sweepRestaurantTimeouts";
    public static final String LOCK_POLL_ACCEPTANCE_TIMEOUTS = "lock:pollAcceptanceTimeouts";
    public static final String LOCK_PROCESS_WEBHOOK_DLQ = "lock:processWebhookDlq";
    public static final String LOCK_RECONCILE_PENDING_PAYMENTS = "lock:reconcilePendingPayments";
}
