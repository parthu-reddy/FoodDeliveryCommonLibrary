package com.fooddelivery.common.dto.ledger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/** What a rider has collected in cash, handed over, and is still holding. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashSummaryDto {
    private UUID driverId;
    private BigDecimal cashCollected;
    private BigDecimal cashRemitted;
    private BigDecimal cashInHand;
}
