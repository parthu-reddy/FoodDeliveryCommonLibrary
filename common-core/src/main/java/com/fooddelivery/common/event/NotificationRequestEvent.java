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
    /**
     * Which notification to send.
     *
     * <p>Typed, not a String. It was a String, and the codes emitted had drifted from the
     * {@code NotificationTemplate} enum entirely: of the fourteen distinct codes the lifecycle
     * emitted, three were enum values, seven were {@code EventType} names and two were bare
     * literals. Nothing could tell whether a code had a template, because nothing could enumerate
     * the codes. A code that is not a NotificationTemplate value now does not compile.
     */
    private com.fooddelivery.common.constants.NotificationTemplate eventName;
    private String explicitRecipient;
    private List<String> templateParams;
    private Map<String, String> payload;
}
