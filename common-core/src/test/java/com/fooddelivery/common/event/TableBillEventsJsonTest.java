package com.fooddelivery.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.config.JacksonConfig;
import com.fooddelivery.common.enums.TableBillStatus;
import jakarta.validation.Validation;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * The two table-bill events (TableBill_2026-11-18 Phase 2) leave unset optional fields off the wire rather than writing
 * null, bind from their own body, and refuse a body missing a required field. The mapper is Boot's plus
 * {@link JacksonConfig}, as in {@code DiningPickupBookingEventBindingTest}.
 */
class TableBillEventsJsonTest {

    private static final Instant AT = Instant.parse("2026-11-19T13:30:00Z");

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

    private static TableBillPaymentEvent.TableBillPaymentEventBuilder completed() {
        return TableBillPaymentEvent.builder().paymentId(UUID.randomUUID()).gatewayOrderId("order_Tb1")
                .amount(new BigDecimal("450.00")).occurredAt(AT);
    }

    private static TableBillChangedEvent changed() {
        return TableBillChangedEvent.builder().sessionId(UUID.randomUUID()).outletId(UUID.randomUUID())
                .amountDue(new BigDecimal("900.00")).amountPaid(new BigDecimal("450.00")).status(TableBillStatus.OPEN)
                .version(4L).occurredAt(AT).build();
    }

    @Test
    void aPaymentEventLeavesItsUnsetOptionalFieldsOffTheWire() {
        JsonNode json = mapper.valueToTree(completed().build());
        assertThat(json.has("gatewayName")).isFalse();
        assertThat(json.has("refundId")).isFalse();
        assertThat(json.has("failureReason")).isFalse();
        assertThat(json.get("amount").decimalValue()).isEqualByComparingTo("450.00");
        assertThat(json.get("occurredAt").isTextual()).isTrue();

        JsonNode failed = mapper.valueToTree(completed().gatewayName("RAZORPAY").failureReason("BAD_REQUEST_ERROR").build());
        assertThat(failed.get("failureReason").asText()).isEqualTo("BAD_REQUEST_ERROR");
        assertThat(failed.has("refundId")).isFalse();
    }

    @Test
    void aChangedEventWithNullFieldsWritesNoNulls() {
        JsonNode json = mapper.valueToTree(TableBillChangedEvent.builder().sessionId(UUID.randomUUID()).build());
        assertThat(json.size()).isEqualTo(1);
        assertThat(json.has("sessionId")).isTrue();
        JsonNode full = mapper.valueToTree(changed());
        assertThat(full.get("status").asText()).isEqualTo("OPEN");
        assertThat(full.get("version").asLong()).isEqualTo(4L);
    }

    @Test
    void bothEventsBindFromTheirOwnBody() throws Exception {
        TableBillPaymentEvent payment = completed().gatewayName("RAZORPAY").refundId("rfnd_1").build();
        assertThat(binder.bind(mapper.writeValueAsString(payment), TableBillPaymentEvent.class)).isEqualTo(payment);
        TableBillChangedEvent bill = changed();
        assertThat(binder.bind(mapper.writeValueAsString(bill), TableBillChangedEvent.class)).isEqualTo(bill);
    }

    @Test
    void aBodyMissingARequiredFieldIsRefused() throws Exception {
        String noPaymentId = mapper.writeValueAsString(completed().paymentId(null).build());
        assertThatThrownBy(() -> binder.bind(noPaymentId, TableBillPaymentEvent.class)).isInstanceOf(EventBindingException.class);
        TableBillChangedEvent noVersion = changed();
        noVersion.setVersion(null);
        String body = mapper.writeValueAsString(noVersion);
        assertThatThrownBy(() -> binder.bind(body, TableBillChangedEvent.class)).isInstanceOf(EventBindingException.class);
    }
}
