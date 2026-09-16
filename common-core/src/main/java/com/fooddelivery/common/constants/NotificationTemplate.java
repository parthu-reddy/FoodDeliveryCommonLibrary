package com.fooddelivery.common.constants;

/**
 * Every notification this platform can send.
 *
 * <p>This enum is the contract with {@code notification_templates}: each value must have a seeded,
 * active row for each channel it is emitted on, and {@code TemplateCoverageTest} fails if one does
 * not. {@code NotificationRequestEvent.eventName} is typed as this enum, so a code that is not a
 * value here does not compile.
 *
 * <p>Before 2026-09-10 the vocabulary was untyped and the seeder was a hand-written list of calls,
 * so the codes seeded and the codes emitted drifted apart: sixteen (code, channel) pairs were
 * seeded, nineteen were emitted, and they were not the same sixteen. Nine emitted pairs -- among
 * them ORDER_PAID, ORDER_READY_FOR_PICKUP and DELAY_APPROVAL_REQUESTED -- had no row and threw
 * {@code InvalidTemplateException} at every customer who needed one, while five seeded rows were
 * for codes nothing emits. Nothing in the build compared the two sets, so nothing could see it.
 *
 * <p>The list is generated from what the source actually emits:
 * {@code tools/collect_emitted_codes.py}. Four values that nothing emitted
 * (ORDER_CANCELLED_DELAY_TIMEOUT, ORDER_ASSIGNED, ORDER_REFUNDED, ORDER_PARTIALLY_REFUNDED) were
 * removed after confirming with that scan.
 */
public enum NotificationTemplate {

    // ---- Order lifecycle. One positional parameter: the order id. -------------------------------

    /** A prepaid order has been paid for. */
    ORDER_PAID(20),
    ORDER_READY_FOR_PICKUP(30),
    DRIVER_ON_THE_WAY(40),
    DELAY_APPROVAL_REQUESTED(50),
    ORDER_DELAY_REJECTED(60),
    ORDER_DELIVERED(70),

    // ---- Endings. Distinct codes because the customer is owed a different explanation. ----------

    ORDER_CANCELLED_BY_RESTAURANT(100),
    ORDER_CANCELLED_BY_ADMIN(110),
    /** No rider could be assigned. The platform's failure, not the restaurant's. */
    DISPATCH_FAILED(120),
    /** The rider had the food and it did not arrive. */
    DELIVERY_FAILED(130),

    // ---- Refunds. -------------------------------------------------------------------------------

    /** Three parameters: order id, amount, destination. */
    REFUND_REQUESTED(200),
    /** Two parameters: order id, amount. */
    PAYMENT_REFUNDED(210),
    /** Two parameters: order id, amount. */
    PAYMENT_PARTIALLY_REFUNDED(220),
    /** Two parameters: order id, amount. */
    REFUND_FAILED(230),

    // ---- Not the order lifecycle, but they share the pipeline and the same failure. -------------

    /** To a driver, offering an order. Carries a payload rather than positional parameters. */
    NEW_ORDER_DISPATCH(300),
    /** To a phone number rather than a user id. */
    OTP_LOGIN(310),
    /** To an advertiser, on EMAIL. */
    AD_CAMPAIGN_PAUSED(320),
    /** To an advertiser, on EMAIL, when a campaign has under 20% of its budget left. */
    BUDGET_RUNNING_LOW(330);

    private final int code;

    NotificationTemplate(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
