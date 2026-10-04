package com.fooddelivery.common.exception;

public class ApplicationRateLimitException extends RuntimeException {
    private final long retryAfter;
    public ApplicationRateLimitException(long retryAfter) {
        super("Too many application changes. Please try again later."); this.retryAfter = retryAfter;
    }
    public long retryAfter() { return retryAfter; }
}
