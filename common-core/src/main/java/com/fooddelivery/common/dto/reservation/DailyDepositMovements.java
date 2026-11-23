package com.fooddelivery.common.dto.reservation;

import java.math.BigDecimal;

/**
 * What a booking's money journal says moved in a reconciliation window, per kind (DiningPickupBooking D54; ARCHITECTURE 4.3,
 * {@code deposit_movements.kind}). reservation-service answers it and {@code DEPOSITS_VS_HELD} compares it with the held
 * account. Every kind is always present, at zero when nothing moved: a missing one is a broken answer, not a zero.
 */
public record DailyDepositMovements(BigDecimal captured, BigDecimal redeemed, BigDecimal shortfallForfeit,
                                    BigDecimal noShowForfeit, BigDecimal lateCancelForfeit, BigDecimal refunded,
                                    BigDecimal creditReturned) {

    /** What the journal says entered the held account: captures, and redeemed credit a table bill gave back (NamedLimits NL-D5). */
    public BigDecimal moneyIn() {
        return java.util.Objects.requireNonNull(captured, "captured")
                .add(java.util.Objects.requireNonNull(creditReturned, "creditReturned"));
    }

    /** What the journal says left the held account. */
    public BigDecimal moneyOut() {
        return java.util.stream.Stream.of(redeemed, shortfallForfeit, noShowForfeit, lateCancelForfeit, refunded)
                .map(part -> java.util.Objects.requireNonNull(part, "a movement kind is missing"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
