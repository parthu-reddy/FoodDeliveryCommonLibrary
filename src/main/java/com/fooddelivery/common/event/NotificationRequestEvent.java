package com.fooddelivery.common.event;

import com.fooddelivery.common.enums.ChannelType;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class NotificationRequestEvent {
    @lombok.Builder.Default
    private String eventId = UUID.randomUUID().toString();
    private UUID userId;
    private ChannelType channel;
    private String eventName;
    private String explicitRecipient;
    private List<String> templateParams;
    private Map<String, String> payload;
























}
