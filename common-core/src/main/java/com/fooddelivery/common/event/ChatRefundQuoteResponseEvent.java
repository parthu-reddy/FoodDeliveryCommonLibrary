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
}
