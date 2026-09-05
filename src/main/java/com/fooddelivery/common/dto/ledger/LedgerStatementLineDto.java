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
public class LedgerStatementLineDto {
    private UUID transactionId;
    private UUID referenceId;
    private ChargeCategory category;
    private BigDecimal amount;
    private TransactionDirection direction;
    private OffsetDateTime createdAt;
    private String description;
    
    private UUID payoutId;
    private String payoutStatus;
    private boolean settled;
}
