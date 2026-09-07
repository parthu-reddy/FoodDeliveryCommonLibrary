package com.fooddelivery.common.dto.ledger;

import java.util.List;
import java.util.UUID;@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.Data


public class LedgerTransactionCommand {
    private UUID transactionId;
    private UUID referenceId;
    private String producer;
    private List<LedgerLeg> legs;

}
