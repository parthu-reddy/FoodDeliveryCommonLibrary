package com.fooddelivery.common.exception;

public class LedgerRejectedException extends RuntimeException {
    public LedgerRejectedException(String message) {
        super(message);
    }

    public LedgerRejectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
