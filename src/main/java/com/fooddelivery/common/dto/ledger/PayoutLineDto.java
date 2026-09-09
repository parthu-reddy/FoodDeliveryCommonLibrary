package com.fooddelivery.common.dto.ledger;

import com.fooddelivery.common.enums.ChargeCategory;
import com.fooddelivery.common.enums.TransactionDirection;
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
public class PayoutLineDto {
    private UUID id;
    private UUID payoutId;
    private UUID ledgerEntryId;
    private UUID referenceId;
    private ChargeCategory category;
    private TransactionDirection direction;
    private BigDecimal amount;
    private OffsetDateTime entryCreatedAt;
    private boolean active;
}
