package com.fooddelivery.common.enums;

/**
 * Why an ORDER_DRIVER_REJECTED was published (DelayPenalties Phase 2, scenario A4). OrderService records it on the released
 * stretch so the delay rules can tell a rider who dropped the order from one who was moved off it (D8).
 */
public enum RiderReleaseReason {
    /** The assigned rider tapped Abort. */
    RIDER_ABORT,
    /** OverdueRiderSweeper released a rider who had not reached the outlet 10 minutes after it was due (D13, D21). */
    OVERDUE_AUTO_REASSIGN,
    /** An administrator moved the order to another rider (DRIVER_ASSIGNED with fromDriverId). */
    ADMIN_REASSIGN,
    /** Nobody answered the ping in time; no rider held the order. */
    PING_TIMEOUT,
    /** The last pinged rider declined; no rider held the order. */
    PING_DECLINED
}
