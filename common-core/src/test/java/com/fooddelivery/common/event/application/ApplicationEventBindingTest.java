package com.fooddelivery.common.event.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fooddelivery.common.constants.EventType;
import com.fooddelivery.common.enums.ApplicationStatus;
import com.fooddelivery.common.event.EventBinder;
import com.fooddelivery.common.event.EventBindingException;
import jakarta.validation.Validation;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApplicationEventBindingTest {
    @Test void typedRootPayloadsPreserveStatusIdentityAndVersion() throws Exception {
        var mapper = new ObjectMapper().findAndRegisterModules();
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var binder = new EventBinder(mapper, factory.getValidator());
            var now = Instant.parse("2026-10-04T00:00:00Z");
            var restaurant = new RestaurantApplicationStatusChangedEvent(UUID.randomUUID(),
                    UUID.randomUUID(), ApplicationStatus.IN_REVIEW, null, 3, now);
            var rider = new DeliveryApplicationStatusChangedEvent(UUID.randomUUID(),
                    ApplicationStatus.REJECTED, "Vehicle document did not pass verification", 5, now);
            assertEquals(restaurant, binder.bindIf(EventType.RESTAURANT_APPLICATION_STATUS_CHANGED,
                    EventType.RESTAURANT_APPLICATION_STATUS_CHANGED.name(), mapper.writeValueAsString(restaurant),
                    RestaurantApplicationStatusChangedEvent.class).orElseThrow());
            ObjectNode payload = mapper.valueToTree(rider);
            payload.put("eventType", EventType.DELIVERY_APPLICATION_STATUS_CHANGED.name());
            assertEquals(rider, binder.bind(payload.toString(), DeliveryApplicationStatusChangedEvent.class));
            assertTrue(binder.bindIf(EventType.DELIVERY_APPLICATION_STATUS_CHANGED,
                    EventType.RESTAURANT_APPLICATION_STATUS_CHANGED.name(), "{}",
                    DeliveryApplicationStatusChangedEvent.class).isEmpty());
        }
    }

    @Test void missingIdentityStatusTimeOrVersionAndNegativeVersionAreRejected() throws Exception {
        var mapper = new ObjectMapper().findAndRegisterModules();
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var binder = new EventBinder(mapper, factory.getValidator());
            var event = new RestaurantApplicationStatusChangedEvent(UUID.randomUUID(), UUID.randomUUID(),
                    ApplicationStatus.DRAFT, null, 0, Instant.parse("2026-10-04T00:00:00Z"));
            for (String field : new String[]{"brandId", "organisationId", "status", "changedAt", "applicationVersion"}) {
                ObjectNode payload = mapper.valueToTree(event);
                payload.remove(field);
                assertThrows(EventBindingException.class, () -> binder.bind(payload.toString(),
                        RestaurantApplicationStatusChangedEvent.class), field);
            }
            ObjectNode negative = mapper.valueToTree(event);
            negative.put("applicationVersion", -1);
            assertThrows(EventBindingException.class, () -> binder.bind(negative.toString(),
                    RestaurantApplicationStatusChangedEvent.class));
            assertThrows(EventBindingException.class, () -> binder.bind("{}", DeliveryApplicationStatusChangedEvent.class));
            assertThrows(EventBindingException.class, () -> binder.bind("not json", DeliveryApplicationStatusChangedEvent.class));
        }
    }
}
