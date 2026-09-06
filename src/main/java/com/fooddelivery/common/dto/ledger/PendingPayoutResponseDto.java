package com.fooddelivery.common.dto.ledger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingPayoutResponseDto {
    private String payeeType;
    private UUID payeeId;
    private String displayName;
    private BigDecimal unsettledAmount;
    private OffsetDateTime unsettledSince;
    private int lineCount;
    private PayoutDto lastPayout;
    private BeneficiaryResponse beneficiaryStatus;
}
