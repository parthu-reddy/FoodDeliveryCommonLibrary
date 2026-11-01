package com.fooddelivery.common.constants;

public enum EventType {
    // Order lifecycle events
    ORDER_CREATED(10),
    ORDER_PAID(20),
    ORDER_ACCEPTED(30),
    ORDER_PREPARING(40),
    ORDER_READY(50),
    ORDER_DELIVERED(60),
    ORDER_REJECTED(70),
    ORDER_AT_RESTAURANT(80),
    /** The rider tapped "I've arrived" at the customer's door (DeliveryStatus.AT_DROP). */
    ORDER_AT_DROP(85),
    /** A rider at the outlet says the food marked ready is not ready yet (DelayPenalties D14). */
    READY_CONTESTED(87),
    ORDER_STATUS_UPDATED(90),
    ORDER_STATUS_SYNC(100),

    // Cancellation events
    ORDER_CANCELLED(110),
    ORDER_CANCELLED_BY_RESTAURANT(120),
    ORDER_CANCELLED_BY_CUSTOMER(130),
    ORDER_CANCELLED_BY_ADMIN(135),

    // Delay approval events
    ORDER_DELAY_APPROVAL_REQUESTED(140),
    ORDER_DELAY_APPROVED(150),
    ORDER_DELAY_REJECTED(160),

    // Dispatch events
    DISPATCH_CANDIDATE_FOUND(170),
    DISPATCH_FAILED(180),
    DRIVER_ASSIGNED(190),
    ORDER_DRIVER_REJECTED(200),
    MANUAL_INTERVENTION_REQUIRED(205),
    FORCE_ASSIGN_DRIVER(206),
    MANUAL_ASSIGNMENT_FAILED(207),

    // Delivery status values
    DELIVERY_FAILED(210),

    // Notification events
    NOTIFICATION_REQUEST(220),
    NOTIFICATION_DISPATCH(225),

    // Payment events
    PAYMENT_WEBHOOK(230),
    PAYMENT_COMPLETED(240),
    PAYMENT_FAILED(250),
    PAYMENT_REFUNDED(260),
    PAYMENT_REFUND_REQUESTED(270),
    PAYMENT_REFUND_FAILED(271),
    REFUND_REQUESTED(272),
    REFUND_FAILED(273),
    PAYMENT_PARTIALLY_REFUNDED(275),
    ORDER_PARTIALLY_REFUNDED(276),
    
    // Ledger events
    LEDGER_TRANSACTION_REQUEST(280),
    
    OUTLET_ACTIVATED(290),
    MENU_UPDATED(295),
    OUTLET_DEACTIVATED(300),

    // Brand events
    BRAND_CREATED(310),

    // Advertisement events
    AD_CAMPAIGN_CREATED(320),
    AD_CAMPAIGN_UPDATED(330),
    AD_CAMPAIGN_PAUSED(340),
    AD_CAMPAIGN_RESUMED(343),
    AD_CAMPAIGN_COMPLETED(344),
    AD_CAMPAIGN_DELETED(346),
    AD_CREATIVE_PENDING(347),
    AD_CREATIVE_APPROVED(348),
    AD_CREATIVE_REJECTED(349),
    AD_CAMPAIGN_BUDGET_EXHAUSTED(350),
    /** Under 20% of today's budget left: CampaignService tells the organisation's ad managers. */
    AD_CAMPAIGN_BUDGET_LOW(352),
    AD_CAMPAIGN_PACING_UPDATED(355),
    AD_IMPRESSION_BILLED(360),
    AD_CLICK_BILLED(370),
    AD_CONVERSION_BILLED(380),
    /** A business wallet top-up's payment was captured: WalletService credits the organisation's wallet. */
    BUSINESS_WALLET_TOPUP_COMPLETED(400),
    /** A business wallet top-up's payment was declined or failed: the top-up ends FAILED, nothing is credited. */
    BUSINESS_WALLET_TOPUP_FAILED(405),
    AD_BUDGET_ALERT(410),

    /**
     * A store-credit refund asking WalletService to credit the customer.
     *
     * <p>Exists so the credit happens outside the refund's own transaction. RefundService used to
     * call the wallet over Feign inside {@code @Transactional}: a rollback after that call left the
     * money credited with no refund row, and the retry minted a fresh refund id, so the wallet's
     * own idempotency could not de-duplicate it and the customer was credited twice.
     */
    WALLET_CREDIT_REQUESTED(420),

    /**
     * A delivered order's delay compensation asking WalletService to credit the customer from platform clearing (DelayPenalties
     * Phase 4): the sum of R1, R3, R4 and R6. Not a refund, so nothing reports back as PAYMENT_REFUNDED.
     */
    DELAY_COMPENSATION_CREDIT_REQUESTED(425),
    
    // Chat events
    CHAT_REFUND_QUOTE_REQUESTED(450),
    CHAT_REFUND_REQUESTED(460),
    CHAT_REFUND_QUOTE_RESPONSE(470),
    CHAT_REFUND_DECISION(480),
    CHAT_REFUND_ERROR(490),
    
    // Review events
    REVIEW_CREATED(500),
    ORGANISATION_CREATED(510),
    ORGANISATION_MEMBERSHIP_CHANGED(520),
    ORGANISATION_STATUS_CHANGED(530),
    RESTAURANT_APPLICATION_STATUS_CHANGED(540),
    DELIVERY_APPLICATION_STATUS_CHANGED(550),

    // Kitchen events
    KITCHEN_STAFF_CHANGED(560),

    // Dine-in events (RandomDocuments/WaiterProfile_2026-10-29)
    /** Every non-void line of a DINE_IN order was served: the order completes without a rider (order-events). */
    ORDER_SERVED(570),
    /**
     * A DINE_IN order the guest pays at the restaurant was placed (DineInPhoneAndCounter D6): the ORDER_PAID snapshot
     * ({@code OrderPaidEvent}, settlement RESTAURANT) with no capture behind it; the kitchen starts (order-events).
     */
    DINE_IN_ORDER_CONFIRMED(575),
    /** A kitchen ticket's per-line ready/void quantities changed; state-carrying, versioned per order (kitchen-events). */
    KITCHEN_ITEM_PROGRESS_CHANGED(580),
    /** A table session opened, changed waiter or closed (dining-events). */
    DINING_SESSION_CHANGED(590);

    private final int code;

    EventType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
