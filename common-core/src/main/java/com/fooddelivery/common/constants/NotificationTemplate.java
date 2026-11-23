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
    /**
     * PUSH to the guest who paid: a refund from their table bill's app payment has completed (NamedLimits NL-D8, RF-L6).
     * One parameter, the amount (₹…): never the outlet name or table label, both user-entered. Sent for a manager's,
     * a surplus and a late refund alike; a counter refund sends none (the guest is at the counter).
     */
    TABLE_BILL_REFUNDED(240),

    // ---- Not the order lifecycle, but they share the pipeline and the same failure. -------------

    /** To a driver, offering an order. Carries a payload rather than positional parameters. */
    NEW_ORDER_DISPATCH(300),
    /** To a phone number rather than a user id. */
    OTP_LOGIN(310),
    /** To an advertiser, on EMAIL. */
    AD_CAMPAIGN_PAUSED(320),
    /** To an advertiser, on EMAIL, when a campaign has under 20% of its budget left. */
    BUDGET_RUNNING_LOW(330),
    APPLICATION_APPROVED(340),
    APPLICATION_REJECTED(350),
    APPLICATION_SUSPENDED(360),
    /**
     * To a phone number: the invitee may not have an account yet. One parameter, the role label.
     * Never carries the organisation or inviter name -- both are user-entered text, and an SMS from
     * the platform sender must not relay arbitrary text to any phone number.
     */
    ORGANISATION_INVITATION(370),
    /** To an organisation's WALLET_TOPUP members, on EMAIL: the business wallet ran dry and its campaigns paused. */
    BUSINESS_WALLET_EMPTY(380),
    /**
     * To a phone number: a waiter seated this guest for a dine-in order, by phone alone (DineInPhoneAndCounter D2). No
     * parameters: never the outlet name or table label, both user-entered (see ORGANISATION_INVITATION).
     */
    DINE_IN_SEATED(390),
    /**
     * PUSH to the guest: a dine-in round was placed and the kitchen has it; the table's bill is paid at the end
     * (TableBill_2026-11-18 D1, D4). ORDER_PAID's copy says "Payment received", which is false for it. One parameter, the
     * order id, as ORDER_PAID.
     */
    DINE_IN_ORDER_CONFIRMED(395),
    /**
     * PUSH, to the outlet's managers: a waiter asked for a served dish to be sent back, and it waits for their approval
     * (NamedLimits NL-D9, SB-L1). No parameters: never the dish name or table label, both user-entered.
     */
    DINING_SEND_BACK_REQUESTED(397),

    // ---- Pickup (DiningPickupBooking Phase 2). PUSH to the customer's account; one parameter, the order id. ----------

    /** The pickup order is ready at the counter; the app shows the customer's handover code from now (D14). */
    PICKUP_READY(500),
    /** The pickup order is still waiting at the counter: sent at most twice while its unclaimed window runs (D16). */
    PICKUP_REMINDER(505),
    /** The pickup order was not collected within the outlet's window and the restaurant has closed it (D16). */
    PICKUP_UNCLAIMED(510),

    // ---- Table booking (DiningPickupBooking Phase 5). No parameters: an SMS never carries user-entered text (D7). --------

    /** PUSH and SMS: a free booking was confirmed. Phase 6's deposit path emits it too. */
    RESERVATION_CONFIRMED(600),
    /** PUSH: the time or party of a booking changed. */
    RESERVATION_MODIFIED(610),
    /** PUSH: the customer cancelled their booking. */
    RESERVATION_CANCELLED_BY_YOU(615),
    /** PUSH: a visit ended. Two parameters: the credit used and the minimum spend not used, both amounts the platform computed. */
    RESERVATION_VISIT_SUMMARY(630),
    /** PUSH: a booking payment arrived that could not be used (too late, the table gone, or paid twice) and is being refunded in full. */
    RESERVATION_DEPOSIT_RETURNED(625),
    /** PUSH: a booking starts soon, at T-3 h and T-30 min. Not sent for a window the booking was made inside (D47). */
    RESERVATION_REMINDER(605),
    /** PUSH and SMS: the restaurant cancelled the booking and refunds in full (D35). */
    RESERVATION_CANCELLED_BY_RESTAURANT(620),
    /** PUSH: the guest did not arrive by the end of grace, so the table was released (D36). */
    RESERVATION_NO_SHOW(635),
    /** PUSH, to the outlet's managers: a booking has no table to move to (D44). */
    RESERVATION_NEEDS_TABLE(637),

    // ---- Platform operations. -------------------------------------------------------------------

    /**
     * To the platform operator, on EMAIL: Prometheus alerts relayed by Alertmanager. Addressed to the configured
     * operator address only, never to a user. One parameter, the HTML body (escaped by the producer); the subject
     * travels in {@code payload["subject"]} because hydration would rewrite a "{2}" inside alert text.
     */
    OPERATOR_ALERT(400);

    private final int code;

    NotificationTemplate(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
