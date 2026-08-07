package com.fooddelivery.common.event;

import com.fooddelivery.common.enums.ChannelType;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NotificationRequestEvent {
    private String eventId;
    private UUID userId;
    private ChannelType channel;
    private String eventName;
    private String explicitRecipient;
    private List<String> templateParams;
    private Map<String, String> payload;

    @java.lang.SuppressWarnings("all")
    private static String $default$eventId() {
        return UUID.randomUUID().toString();
    }


    @java.lang.SuppressWarnings("all")
    public static class NotificationRequestEventBuilder {
        @java.lang.SuppressWarnings("all")
        private boolean eventId$set;
        @java.lang.SuppressWarnings("all")
        private String eventId$value;
        @java.lang.SuppressWarnings("all")
        private UUID userId;
        @java.lang.SuppressWarnings("all")
        private ChannelType channel;
        @java.lang.SuppressWarnings("all")
        private String eventName;
        @java.lang.SuppressWarnings("all")
        private String explicitRecipient;
        @java.lang.SuppressWarnings("all")
        private List<String> templateParams;
        @java.lang.SuppressWarnings("all")
        private Map<String, String> payload;

        @java.lang.SuppressWarnings("all")
        NotificationRequestEventBuilder() {
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public NotificationRequestEvent.NotificationRequestEventBuilder eventId(final String eventId) {
            this.eventId$value = eventId;
            eventId$set = true;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public NotificationRequestEvent.NotificationRequestEventBuilder userId(final UUID userId) {
            this.userId = userId;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public NotificationRequestEvent.NotificationRequestEventBuilder channel(final ChannelType channel) {
            this.channel = channel;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public NotificationRequestEvent.NotificationRequestEventBuilder eventName(final String eventName) {
            this.eventName = eventName;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public NotificationRequestEvent.NotificationRequestEventBuilder explicitRecipient(final String explicitRecipient) {
            this.explicitRecipient = explicitRecipient;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public NotificationRequestEvent.NotificationRequestEventBuilder templateParams(final List<String> templateParams) {
            this.templateParams = templateParams;
            return this;
        }

        /**
         * @return {@code this}.
         */
        @java.lang.SuppressWarnings("all")
        public NotificationRequestEvent.NotificationRequestEventBuilder payload(final Map<String, String> payload) {
            this.payload = payload;
            return this;
        }

        @java.lang.SuppressWarnings("all")
        public NotificationRequestEvent build() {
            String eventId$value = this.eventId$value;
            if (!this.eventId$set) eventId$value = NotificationRequestEvent.$default$eventId();
            return new NotificationRequestEvent(eventId$value, this.userId, this.channel, this.eventName, this.explicitRecipient, this.templateParams, this.payload);
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        public java.lang.String toString() {
            return "NotificationRequestEvent.NotificationRequestEventBuilder(eventId$value=" + this.eventId$value + ", userId=" + this.userId + ", channel=" + this.channel + ", eventName=" + this.eventName + ", explicitRecipient=" + this.explicitRecipient + ", templateParams=" + this.templateParams + ", payload=" + this.payload + ")";
        }
    }

    @java.lang.SuppressWarnings("all")
    public static NotificationRequestEvent.NotificationRequestEventBuilder builder() {
        return new NotificationRequestEvent.NotificationRequestEventBuilder();
    }

    @java.lang.SuppressWarnings("all")
    public String getEventId() {
        return this.eventId;
    }

    @java.lang.SuppressWarnings("all")
    public UUID getUserId() {
        return this.userId;
    }

    @java.lang.SuppressWarnings("all")
    public ChannelType getChannel() {
        return this.channel;
    }

    @java.lang.SuppressWarnings("all")
    public String getEventName() {
        return this.eventName;
    }

    @java.lang.SuppressWarnings("all")
    public String getExplicitRecipient() {
        return this.explicitRecipient;
    }

    @java.lang.SuppressWarnings("all")
    public List<String> getTemplateParams() {
        return this.templateParams;
    }

    @java.lang.SuppressWarnings("all")
    public Map<String, String> getPayload() {
        return this.payload;
    }

    @java.lang.SuppressWarnings("all")
    public void setEventId(final String eventId) {
        this.eventId = eventId;
    }

    @java.lang.SuppressWarnings("all")
    public void setUserId(final UUID userId) {
        this.userId = userId;
    }

    @java.lang.SuppressWarnings("all")
    public void setChannel(final ChannelType channel) {
        this.channel = channel;
    }

    @java.lang.SuppressWarnings("all")
    public void setEventName(final String eventName) {
        this.eventName = eventName;
    }

    @java.lang.SuppressWarnings("all")
    public void setExplicitRecipient(final String explicitRecipient) {
        this.explicitRecipient = explicitRecipient;
    }

    @java.lang.SuppressWarnings("all")
    public void setTemplateParams(final List<String> templateParams) {
        this.templateParams = templateParams;
    }

    @java.lang.SuppressWarnings("all")
    public void setPayload(final Map<String, String> payload) {
        this.payload = payload;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof NotificationRequestEvent)) return false;
        final NotificationRequestEvent other = (NotificationRequestEvent) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        final java.lang.Object this$eventId = this.getEventId();
        final java.lang.Object other$eventId = other.getEventId();
        if (this$eventId == null ? other$eventId != null : !this$eventId.equals(other$eventId)) return false;
        final java.lang.Object this$userId = this.getUserId();
        final java.lang.Object other$userId = other.getUserId();
        if (this$userId == null ? other$userId != null : !this$userId.equals(other$userId)) return false;
        final java.lang.Object this$channel = this.getChannel();
        final java.lang.Object other$channel = other.getChannel();
        if (this$channel == null ? other$channel != null : !this$channel.equals(other$channel)) return false;
        final java.lang.Object this$eventName = this.getEventName();
        final java.lang.Object other$eventName = other.getEventName();
        if (this$eventName == null ? other$eventName != null : !this$eventName.equals(other$eventName)) return false;
        final java.lang.Object this$explicitRecipient = this.getExplicitRecipient();
        final java.lang.Object other$explicitRecipient = other.getExplicitRecipient();
        if (this$explicitRecipient == null ? other$explicitRecipient != null : !this$explicitRecipient.equals(other$explicitRecipient)) return false;
        final java.lang.Object this$templateParams = this.getTemplateParams();
        final java.lang.Object other$templateParams = other.getTemplateParams();
        if (this$templateParams == null ? other$templateParams != null : !this$templateParams.equals(other$templateParams)) return false;
        final java.lang.Object this$payload = this.getPayload();
        final java.lang.Object other$payload = other.getPayload();
        if (this$payload == null ? other$payload != null : !this$payload.equals(other$payload)) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof NotificationRequestEvent;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $eventId = this.getEventId();
        result = result * PRIME + ($eventId == null ? 43 : $eventId.hashCode());
        final java.lang.Object $userId = this.getUserId();
        result = result * PRIME + ($userId == null ? 43 : $userId.hashCode());
        final java.lang.Object $channel = this.getChannel();
        result = result * PRIME + ($channel == null ? 43 : $channel.hashCode());
        final java.lang.Object $eventName = this.getEventName();
        result = result * PRIME + ($eventName == null ? 43 : $eventName.hashCode());
        final java.lang.Object $explicitRecipient = this.getExplicitRecipient();
        result = result * PRIME + ($explicitRecipient == null ? 43 : $explicitRecipient.hashCode());
        final java.lang.Object $templateParams = this.getTemplateParams();
        result = result * PRIME + ($templateParams == null ? 43 : $templateParams.hashCode());
        final java.lang.Object $payload = this.getPayload();
        result = result * PRIME + ($payload == null ? 43 : $payload.hashCode());
        return result;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "NotificationRequestEvent(eventId=" + this.getEventId() + ", userId=" + this.getUserId() + ", channel=" + this.getChannel() + ", eventName=" + this.getEventName() + ", explicitRecipient=" + this.getExplicitRecipient() + ", templateParams=" + this.getTemplateParams() + ", payload=" + this.getPayload() + ")";
    }

    @java.lang.SuppressWarnings("all")
    public NotificationRequestEvent() {
        this.eventId = NotificationRequestEvent.$default$eventId();
    }

    @java.lang.SuppressWarnings("all")
    public NotificationRequestEvent(final String eventId, final UUID userId, final ChannelType channel, final String eventName, final String explicitRecipient, final List<String> templateParams, final Map<String, String> payload) {
        this.eventId = eventId;
        this.userId = userId;
        this.channel = channel;
        this.eventName = eventName;
        this.explicitRecipient = explicitRecipient;
        this.templateParams = templateParams;
        this.payload = payload;
    }
}
