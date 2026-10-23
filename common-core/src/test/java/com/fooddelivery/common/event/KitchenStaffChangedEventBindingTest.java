package com.fooddelivery.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.config.JacksonConfig;
import com.fooddelivery.common.enums.KitchenRole;
import com.fooddelivery.common.enums.KitchenStaffStatus;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KITCHEN_STAFF_CHANGED binds from its real body; identity can never project a row without a valid phone or roles.
 *
 * <p>The mapper comes from Boot's {@link JacksonAutoConfiguration} plus {@link JacksonConfig}, as in
 * {@code JacksonTimeWireFormatTest}: a bare builder keeps WRITE_DATES_AS_TIMESTAMPS on and writes {@code occurredAt}
 * as an epoch number, a format production never sends.
 */
class KitchenStaffChangedEventBindingTest {

    private EventBinder binder;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        AtomicReference<ObjectMapper> boot = new AtomicReference<>();
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
                .withUserConfiguration(JacksonConfig.class).run(ctx -> boot.set(ctx.getBean(ObjectMapper.class)));
        mapper = boot.get();
        binder = new EventBinder(mapper, Validation.buildDefaultValidatorFactory().getValidator());
    }

    private KitchenStaffChangedEvent sample() {
        return KitchenStaffChangedEvent.builder().staffId(UUID.randomUUID()).organisationId(UUID.randomUUID())
                .outletId(UUID.randomUUID()).phoneNumber("8000000505").userId(null).roles(Set.of(KitchenRole.CHEF))
                .status(KitchenStaffStatus.INVITED).staffVersion(1).occurredAt(Instant.parse("2026-10-20T12:00:00Z")).build();
    }

    @Test
    void theSerializedEventBindsBack() throws Exception {
        KitchenStaffChangedEvent sent = sample();
        String json = mapper.writeValueAsString(sent);
        assertTrue(json.contains("\"occurredAt\":\"2026-10-20T12:00:00Z\""), json);
        assertEquals(sent, binder.bind(json, KitchenStaffChangedEvent.class));
    }

    @Test
    void aBadPhoneOrNoRolesIsRefused() throws Exception {
        KitchenStaffChangedEvent badPhone = sample();
        badPhone.setPhoneNumber("+919876543210");
        assertThrows(EventBindingException.class, () -> binder.bind(mapper.writeValueAsString(badPhone), KitchenStaffChangedEvent.class));
        KitchenStaffChangedEvent noRoles = sample();
        noRoles.setRoles(Set.of());
        assertThrows(EventBindingException.class, () -> binder.bind(mapper.writeValueAsString(noRoles), KitchenStaffChangedEvent.class));
        KitchenStaffChangedEvent noStatus = sample();
        noStatus.setStatus(null);
        assertThrows(EventBindingException.class, () -> binder.bind(mapper.writeValueAsString(noStatus), KitchenStaffChangedEvent.class));
    }
}
