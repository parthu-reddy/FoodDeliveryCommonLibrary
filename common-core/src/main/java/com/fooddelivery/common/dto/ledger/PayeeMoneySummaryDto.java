package com.fooddelivery.common.dto.ledger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * What one payee -- a single restaurant or rider -- is owed and when they were last paid.
 *
 * <p>Served to the SERVICE identity so the restaurant and rider summary cards can be built without
 * reading the admin-only queue of every payee on the platform.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayeeMoneySummaryDto {
    private String payeeType;
    private UUID payeeId;
    /** Earned, not yet covered by a DRAFT or APPROVED payout. */
    private BigDecimal unsettledAmount;
    /** Already inside a payout that has been raised but not paid. */
    private BigDecimal pendingPayoutAmount;
    private PayoutDto lastPayout;
    private BeneficiaryResponse beneficiary;
}
