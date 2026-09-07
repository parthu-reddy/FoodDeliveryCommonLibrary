package com.fooddelivery.common.dto.ledger;

import com.fooddelivery.common.enums.ChargeCategory;
import com.fooddelivery.common.enums.LedgerAccountType;

import java.math.BigDecimal;
import java.util.UUID;@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Data


public class LedgerLeg {
    private LedgerAccountType fromType;
    private UUID fromId;
    private LedgerAccountType toType;
    private UUID toId;
    private BigDecimal amount;
    private ChargeCategory category;
    private String description;
    private String authorizedBy;

}
