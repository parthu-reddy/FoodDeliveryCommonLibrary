package com.fooddelivery.common.dto.ledger;

import com.fooddelivery.common.enums.ChargeCategory;
import com.fooddelivery.common.enums.LedgerAccountType;

import java.math.BigDecimal;
import java.util.UUID;

public class LedgerLeg {
    private LedgerAccountType fromType;
    private UUID fromId;
    private LedgerAccountType toType;
    private UUID toId;
    private BigDecimal amount;
    private ChargeCategory category;
    private String description;
    private String authorizedBy;

    public LedgerLeg() {}

    public LedgerLeg(LedgerAccountType fromType, UUID fromId, LedgerAccountType toType, UUID toId, BigDecimal amount, ChargeCategory category, String description, String authorizedBy) {
        this.fromType = fromType;
        this.fromId = fromId;
        this.toType = toType;
        this.toId = toId;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.authorizedBy = authorizedBy;
    }

    public LedgerAccountType getFromType() { return fromType; }
    public void setFromType(LedgerAccountType fromType) { this.fromType = fromType; }

    public UUID getFromId() { return fromId; }
    public void setFromId(UUID fromId) { this.fromId = fromId; }

    public LedgerAccountType getToType() { return toType; }
    public void setToType(LedgerAccountType toType) { this.toType = toType; }

    public UUID getToId() { return toId; }
    public void setToId(UUID toId) { this.toId = toId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public ChargeCategory getCategory() { return category; }
    public void setCategory(ChargeCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAuthorizedBy() { return authorizedBy; }
    public void setAuthorizedBy(String authorizedBy) { this.authorizedBy = authorizedBy; }
}
