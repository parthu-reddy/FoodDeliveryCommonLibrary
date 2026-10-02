package com.fooddelivery.common.event;

import java.math.BigDecimal;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRefundQuoteResponseEvent {
    private BigDecimal quoteAmount;
    private String refundType;
    /** The validated selection must survive the quote-to-request round trip. */
    private java.util.UUID orderId;
    private java.util.List<ChatRefundRequestedEvent.Item> items;
    private String reason;
}
