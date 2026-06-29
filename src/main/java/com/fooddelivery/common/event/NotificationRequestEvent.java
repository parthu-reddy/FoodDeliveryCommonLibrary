package com.fooddelivery.common.event;

import com.fooddelivery.common.enums.ChannelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestEvent {
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();
    private UUID userId;
    private ChannelType channel;
    private String eventName;
    private String explicitRecipient;
    private List<String> templateParams;
    private Map<String, String> payload;
}
