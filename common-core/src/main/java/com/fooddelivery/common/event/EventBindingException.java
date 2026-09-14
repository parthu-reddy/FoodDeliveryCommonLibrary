package com.fooddelivery.common.event;

/**
 * A message that can never be processed: it does not parse, or it violates the event's constraints.
 *
 * <p>Exists to be named in {@code @RetryableTopic(exclude = ...)}. Retrying a malformed payload is
 * pure waste — the bytes do not change between attempts — so a binding failure should reach the DLT
 * immediately rather than after three to five backed-off retries. The same reasoning the codebase
 * already applies to {@code TerminalNotificationException} and {@code InsufficientFundsException}.
 *
 * <p>Extends {@link IllegalArgumentException} deliberately: a payload the consumer cannot bind is an
 * illegal argument, and the consumers that already convert an {@code IllegalArgumentException} into
 * a business response — {@code ChatRefundProcessorService} turns one into a CHAT_REFUND_ERROR for
 * the customer — keep doing exactly the right thing without a second catch.
 *
 * <p><strong>A consumer that catches this and rethrows it wrapped in a plain
 * {@code RuntimeException} defeats the exclusion silently</strong>, because Spring Kafka matches on
 * the thrown type. Nine consumers did that before this class existed. {@code EVENT-BIND-NO-RETRY} in
 * validate_core_services.py checks both halves: that the exclusion is declared, and that no generic
 * catch swallows the type on the way out.
 */
public class EventBindingException extends IllegalArgumentException {

    public EventBindingException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventBindingException(String message) {
        super(message);
    }
}
