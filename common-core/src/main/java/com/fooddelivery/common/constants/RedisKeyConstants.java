package com.fooddelivery.common.constants;

public final class RedisKeyConstants {
    private RedisKeyConstants() {}

    public static final String PREFIX_ORDER_DRIVER_LOCK = "order:driver:lock:";
    public static final String PREFIX_DRIVER_PENDING_PING = "driver:pending_ping:";
    public static final String PREFIX_ORDER_RESTAURANT_STATUS = "order:restaurantStatus:";
    public static final String PREFIX_ORDER_DISPATCH_LOCK = "order:dispatch:lock:";
    public static final String PREFIX_ORDER_DISPATCH_PAYLOAD = "order:dispatchPayload:";
    public static final String PREFIX_DRIVER_ACTIVE_ORDER = "driver:active_order:";
    
    /**
     * Set of drivers currently free to be dispatched in a city, keyed by cityId.
     * Candidate selection removes from it with an atomic SREM; release and
     * re-reserve are the only ways back in. Shared because both MapsIntegration and
     * DeliveryExecutiveApplication address this set, and a drift between their
     * spellings strands drivers silently.
     */
    public static final String PREFIX_DRIVERS_AVAILABLE = "drivers:available:";

    public static final String PREFIX_ORDER_PING_PENDING = "order:ping:pending:";
    /**
     * Drivers whose dispatch ping was confirmed delivered to a live socket, per order.
     * A driver absent from this set was never shown the order, so letting it lapse is not a
     * rejection and must not count against them.
     */
    public static final String PREFIX_ORDER_PING_REACHED = "order:ping:reached:";
    public static final String PREFIX_ORDER_PING_TIMEOUTS = "order:ping:timeouts";
    public static final String PREFIX_ORDER_REJECTED_DRIVERS = "order:rejected_drivers:";
    
    // Advertisement Service
    public static final String PREFIX_AD_CAMPAIGN_PACING = "campaign:%s:pacing";
    public static final String PREFIX_AD_CAMPAIGN_MAX_BID = "campaign:%s:maxBid";
    public static final String PREFIX_AD_CAMPAIGN_ADVERTISER = "campaign:%s:advertiserId";
    public static final String PREFIX_AD_WALLET_BALANCE = "wallet:%s:balance";
    public static final String KEY_ACTIVE_CAMPAIGNS = "campaigns:active";
    
    public static final String LOCK_POLL_DELAYED_DISPATCHES = "lock:pollDelayedDispatches";
    public static final String LOCK_POLL_PING_TIMEOUTS = "lock:pollPingTimeouts";
    public static final String LOCK_SWEEP_STALE_DRIVERS = "lock:sweepStaleDrivers";
    public static final String LOCK_SWEEP_ABANDONED_DELIVERIES = "lock:sweepAbandonedDeliveries";
    public static final String LOCK_SWEEP_REFUND_RETRIES = "lock:sweepRefundRetries";
    /** Nightly money reconciliation: one replica runs it, the rest skip. */
    public static final String LOCK_MONEY_RECONCILIATION = "lock:moneyReconciliation";
    public static final String LOCK_SWEEP_STALE_CREATED_ORDERS = "lock:sweepStaleCreatedOrders";
    public static final String LOCK_SWEEP_RESTAURANT_TIMEOUTS = "lock:sweepRestaurantTimeouts";
    public static final String LOCK_POLL_ACCEPTANCE_TIMEOUTS = "lock:pollAcceptanceTimeouts";
    public static final String LOCK_PROCESS_WEBHOOK_DLQ = "lock:processWebhookDlq";
    public static final String LOCK_RECONCILE_PENDING_PAYMENTS = "lock:reconcilePendingPayments";
    public static final String LOCK_REAPER_TASK = "lock:reaper_task_execution";

    /** Sorted set of orders waiting to be re-dispatched, scored by the time they become due. */
    public static final String QUEUE_DELAYED_DISPATCH = "delayed_dispatch_queue";
}
