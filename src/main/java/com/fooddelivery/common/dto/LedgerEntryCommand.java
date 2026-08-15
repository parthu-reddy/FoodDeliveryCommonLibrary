package com.fooddelivery.common.dto;

import com.fooddelivery.common.enums.AccountType;
import com.fooddelivery.common.enums.ChargeCategory;
import java.math.BigDecimal;
import java.util.UUID;

public class LedgerEntryCommand {
    private UUID fromId;
    private AccountType fromType;
    private UUID toId;
    private AccountType toType;
    private BigDecimal amount;
    private ChargeCategory category;

    public LedgerEntryCommand() {}

    public LedgerEntryCommand(UUID fromId, AccountType fromType, UUID toId, AccountType toType, BigDecimal amount, ChargeCategory category) {
        this.fromId = fromId;
        this.fromType = fromType;
        this.toId = toId;
        this.toType = toType;
        this.amount = amount;
        this.category = category;
    }

    public UUID getFromId() { return fromId; }
    public void setFromId(UUID fromId) { this.fromId = fromId; }

    public AccountType getFromType() { return fromType; }
    public void setFromType(AccountType fromType) { this.fromType = fromType; }

    public UUID getToId() { return toId; }
    public void setToId(UUID toId) { this.toId = toId; }

    public AccountType getToType() { return toType; }
    public void setToType(AccountType toType) { this.toType = toType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public ChargeCategory getCategory() { return category; }
    public void setCategory(ChargeCategory category) { this.category = category; }
}
