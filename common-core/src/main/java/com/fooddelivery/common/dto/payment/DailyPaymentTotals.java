package com.fooddelivery.common.dto.payment;

import com.fooddelivery.common.enums.PaymentPurpose;

import java.math.BigDecimal;
import java.util.Map;

/**
 * What the gateway captured and refunded in a reconciliation window. {@code GATEWAY_VS_LEDGER} compares the all-purpose totals,
 * because deposits move the gateway receivable too (DiningPickupBooking D54). {@code byPurpose} is for an operator reading a
 * break; every purpose is present, at zero when it moved nothing.
 */
public record DailyPaymentTotals(BigDecimal capturedAmount, BigDecimal refundedAmount, Map<PaymentPurpose, PurposeTotals> byPurpose) {

    public record PurposeTotals(BigDecimal capturedAmount, BigDecimal refundedAmount) {}
}
