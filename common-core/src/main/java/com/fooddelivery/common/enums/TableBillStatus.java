package com.fooddelivery.common.enums;

/** A table visit's bill (TableBill_2026-11-18 D6, D12). A round placed on a SETTLED bill opens it again. */
public enum TableBillStatus {
    /** Something is still due. */
    OPEN,
    /** The payments sum to the amount due. */
    SETTLED
}
