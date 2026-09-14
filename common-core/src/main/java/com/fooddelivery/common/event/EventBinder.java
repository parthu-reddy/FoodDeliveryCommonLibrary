package com.fooddelivery.common.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.constants.EventType;
import com.fooddelivery.common.util.EventPayloadUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * Binds a Kafka message body to a typed event class, so field names are compile-time symbols
 * instead of string keys recovered by regex.
 *
 * <p>Shape to use at a listener: resolve the event type from the Kafka header
 * ({@code KafkaHeaderUtils.extractEventType}), then call {@link #bindIf} per branch. The header is
 * trustworthy because {@code OutboxProcessor} publishes it from the outbox row and rejects, at that
 * single publish path, any body {@code eventType} that contradicts it (ADR 002).
 */
@Component
@RequiredArgsConstructor
public class EventBinder {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    /**
     * Binds the message to {@code type} when {@code resolvedType} is the event this branch handles.
     *
     * @return the bound event, or empty when this is a different event type on the same topic.
     *         Empty NEVER means "binding failed" -- a malformed body or a violated constraint
     *         throws, so the retry/DLT machinery sees it. Silently returning empty would recreate
     *         the drop-without-a-trace failure typed binding exists to remove.
     */
    public <T> Optional<T> bindIf(EventType expected, String resolvedType, String payload, Class<T> type) {
        if (expected == null || resolvedType == null || !expected.name().equals(resolvedType)) {
            return Optional.empty();
        }
        return Optional.of(bind(payload, type));
    }

    /**
     * Binds the message body to {@code type}.
     *
     * <p>Binds the ROOT of the message, deliberately. It does not call
     * {@link EventPayloadUtils#unwrapPayload} first: no producer on any Kafka topic in this platform
     * emits an {@code {eventType, payload}} envelope -- every one publishes the business object flat
     * through the outbox, and the only {@code set("payload", ...)} in the workspace is a mock
     * gateway's HTTP webhook body.
     *
     * <p>Unwrapping here was actively wrong. {@code unwrapPayload} returns {@code root.get("payload")}
     * whenever that key is present, and {@link NotificationRequestEvent} HAS a {@code payload} field
     * of its own. So an OTP login event published by IdentityService -- which sets
     * {@code payload(Map.of("otp", otp))} -- unwrapped to the inner {@code {"otp":"..."}} map and
     * bound to a NotificationRequestEvent with eventName, channel and recipient all null. Every OTP
     * SMS was silently destroyed. See EventBinderPayloadCollisionTest, which fails if this is
     * reintroduced.
     */
    public <T> T bind(String payload, Class<T> type) {
        final T parsed;
        try {
            parsed = objectMapper.readValue(payload, type);
        } catch (Exception e) {
            throw new EventBindingException(
                    "Failed to bind event payload to " + type.getSimpleName(), e);
        }
        if (parsed == null) {
            // readValue returns null for the JSON literal `null` (and a stubbed mapper does too).
            // Passing that to the validator throws "HV000116: The object to be validated must not
            // be null", which says nothing about the message that caused it.
            throw new EventBindingException(
                    "Event payload deserialised to null for " + type.getSimpleName());
        }
        Set<ConstraintViolation<T>> violations = validator.validate(parsed);
        if (!violations.isEmpty()) {
            // Outside the try: a ConstraintViolationException is the answer, not a binding failure,
            // and wrapping it would hide which field was missing from the DLT record.
            // Wrapped, not raw: a ConstraintViolationException cannot be named in
            // @RetryableTopic(exclude) alongside parse failures without listing two types at every
            // consumer, and a violated constraint is just as unfixable by retrying. The violations
            // stay on the message so the DLT record still says which field was wrong.
            throw new EventBindingException(
                    "Event payload failed validation for " + type.getSimpleName() + ": "
                            + new ConstraintViolationException(violations).getMessage(),
                    new ConstraintViolationException(violations));
        }
        return parsed;
    }

}
