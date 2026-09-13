package com.fooddelivery.common.dto.ledger;

import com.fooddelivery.common.enums.ChargeCategory;
import com.fooddelivery.common.enums.LedgerAccountType;
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
    private UUID accountId;
    /**
     * The owner the account belongs to -- a restaurant, driver, customer or gateway id. Callers that
     * need to attribute a line to a party must match on this, not on {@link #accountId}, which is the
     * ledger account's own surrogate key. Matching a party id against accountId silently matches
     * nothing, which is how the clawback cap came to never apply.
     */
    private UUID ownerId;
    private LedgerAccountType ownerType;
    private ChargeCategory category;
    private BigDecimal amount;
    private TransactionDirection direction;
    private OffsetDateTime createdAt;
    private String description;
    
    private UUID payoutId;
    private String payoutStatus;
    private boolean settled;
}
