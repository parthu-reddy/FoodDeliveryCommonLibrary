package com.fooddelivery.common.event;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRefundDecisionEvent {
    private String status;
    private String ticketId;
    private String message;
}
