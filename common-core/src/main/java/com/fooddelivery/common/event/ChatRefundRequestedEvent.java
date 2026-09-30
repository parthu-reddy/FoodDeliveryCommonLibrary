package com.fooddelivery.common.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * A refund asked for from inside a chat session.
 *
 * <p>The customer-visible fields are supplied by the chat client. {@code ChatMessageService}
 * stamps {@link #actorId} and {@link #actorType} from the authenticated canonical chat sender
 * before publishing the event. Consumers must use those server-owned fields to authorize the
 * financial request and must never trust {@link #customerId} from the client payload.
 *
 * <p>{@code orderId} and the item ids are {@code UUID}, not {@code String}, so a malformed id is
 * rejected at bind time instead of throwing from {@code UUID.fromString} somewhere further in.
 * {@code ChatRefundProcessorService} turns both outcomes into a CHAT_REFUND_ERROR event for the
 * customer rather than a DLT entry.
 *
 * <p>Wire shape tolerates fields it does not declare — {@code eventType} above all. Repeating
 * eventType in the body is this platform's convention (EventPayloadUtils records 28 production call
 * sites and 6 message contracts), and OutboxProcessor enforces only that it must not CONTRADICT the
 * outbox row (ADR 002). Declared on the class rather than left to the ObjectMapper's global
 * setting: Boot's mapper disables FAIL_ON_UNKNOWN_PROPERTIES but a plain {@code new ObjectMapper()}
 * does not, and several test harnesses build one.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRefundRequestedEvent {

    @NotNull(message = "orderId is required")
    private UUID orderId;

    /**
     * Required for CHAT_REFUND_REQUESTED, absent for CHAT_REFUND_QUOTE_REQUESTED — so it is checked
     * by the handler that needs it rather than annotated here, which would reject valid quotes.
     */
    private UUID customerId;

    /**
     * Server-owned identity of the authenticated chat sender. It is written by
     * {@code ChatMessageService}, replacing any value supplied by the browser.
     */
    private String actorId;

    /**
     * Server-owned canonical participant type of {@link #actorId}. A customer is the only actor
     * permitted to initiate a refund quote or refund request.
     */
    private String actorType;

    /** FULL or PARTIAL; the handlers default to FULL when absent. */
    private String refundType;

    private String reason;

    private String description;

    /** {@code @Valid} so a malformed item is rejected with the rest, not on first read. */
    @Valid
    private List<Item> items;

    /** One line of a partial refund request. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        @NotNull(message = "itemId is required on a refund item")
        private UUID itemId;

        @NotNull(message = "quantity is required on a refund item")
        @Positive(message = "quantity must be greater than zero")
        private Integer quantity;
    }
}
