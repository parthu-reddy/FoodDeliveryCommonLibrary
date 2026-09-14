package com.fooddelivery.common.event;

import java.util.UUID;

/**
 * An event that is about one order.
 *
 * <p>Exists so a listener can get the order id off a bound event without reflection. The first cut
 * of typed binding did this instead:
 *
 * <pre>
 *   Method m = typedEvent.getClass().getMethod("getOrderId");
 *   Object val = m.invoke(typedEvent);
 * </pre>
 *
 * with an empty catch underneath. That is the same defect typed binding was meant to remove -- a
 * field name as an unchecked string, failing at runtime -- only slower, and invisible to the
 * string-key audit because it is not a {@code .get("...")}. Renaming {@code orderId} would still
 * compile and still break at runtime. Implementing this interface makes it a compile error.
 *
 * <p>{@code orderUuid()} rather than a getter: the field is {@code String} on most of these classes
 * and {@code UUID} on {@link OrderPaidEvent} and {@link OrderDelayApprovedEvent}, and every consumer
 * wants the UUID. The name is deliberately not {@code getOrderUuid} so Jackson's bean introspection
 * does not treat it as a property and add an {@code orderUuid} key to the wire shape --
 * EventSerializationTest asserts that it does not.
 */
public interface OrderScopedEvent {

    /**
     * @return the order this event concerns, or null when the payload carried no order id.
     * @throws IllegalArgumentException if the payload carried an order id that is not a UUID.
     */
    UUID orderUuid();
}
