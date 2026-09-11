package com.fooddelivery.common.enums;

/**
 * Where an order is in its life.
 *
 * <p>The sequence is monotonic and {@code Order.setStatus} refuses to move backward through it.
 * Everything at 100 is terminal.
 *
 * <p>There is deliberately no DELIVERED: a completed delivery is {@code DeliveryStatus.DELIVERED}
 * on the order's other status field, and HANDED_OVER is the last non-terminal value here.
 */
public enum OrderStatus {
    CREATED(10),
    PENDING_ACCEPTANCE(20),
    AWAITING_DELAY_APPROVAL(30),
    ACCEPTED(40),
    PREPARING(50),
    READY_FOR_PICKUP(60),
    HANDED_OVER(70),

    /** The customer, or an administrator on their behalf, ended it. */
    CANCELLED(100),
    /** The restaurant would not or could not fulfil it. Drives a RESTAURANT_FAULT clawback. */
    CANCELLED_BY_RESTAURANT(100),
    /**
     * The platform could not deliver it -- no driver found, dispatch abandoned, manual intervention
     * that timed out.
     *
     * <p>Exists because these used to be cancelled as CANCELLED_BY_RESTAURANT by the 60-minute
     * stuck-order sweeper, an hour after the fact, which billed the restaurant for the platform's
     * failure to find a driver.
     */
    CANCELLED_BY_PLATFORM(100),
    /** The rider had the food and it did not arrive. Terminal; the customer is refunded. */
    DELIVERY_FAILED(100);

    private static final int TERMINAL_SEQUENCE = 100;

    private final int sequence;

    OrderStatus(int sequence) {
        this.sequence = sequence;
    }

    public int getSequence() {
        return sequence;
    }

    /**
     * Whether the order is over.
     *
     * <p>The single definition. This was previously inferred four different ways in four places --
     * a private helper in the saga orchestrator, the restaurant module's own enum, the terminal
     * entries of {@code OrderStateFactory}, and a hand-listed REFUND_STATUSES constant.
     */
    public boolean isTerminal() {
        return sequence == TERMINAL_SEQUENCE;
    }
}
