package com.fooddelivery.common.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.config.JacksonConfig;
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
 * NamedLimits NL-D1: a deposit's and a table bill's outcome carry our refund id and the provider's apart, and
 * {@code reportsRefund()} is true when either is set. A refused initiation has only ours; a provider's report always has its
 * own; a refund started at the provider has none of ours. The helper is not written to the wire.
 */
class RefundIdsOnPaymentEventsTest {

    private static final Instant AT = Instant.parse("2026-11-22T12:00:00Z");
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        AtomicReference<ObjectMapper> boot = new AtomicReference<>();
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
                .withUserConfiguration(JacksonConfig.class).run(ctx -> boot.set(ctx.getBean(ObjectMapper.class)));
        mapper = boot.get();
    }

    private static TableBillPaymentEvent.TableBillPaymentEventBuilder bill() {
        return TableBillPaymentEvent.builder().paymentId(UUID.randomUUID()).gatewayOrderId("order_1")
                .amount(BigDecimal.TEN).occurredAt(AT);
    }

    private static ReservationPaymentEvent.ReservationPaymentEventBuilder deposit() {
        return ReservationPaymentEvent.builder().referenceId("dep-1").gatewayOrderId("order_2")
                .amount(BigDecimal.TEN).occurredAt(AT);
    }

    @Test
    void aCaptureOutcomeReportsNoRefund() {
        assertThat(bill().build().reportsRefund()).isFalse();
        assertThat(deposit().build().reportsRefund()).isFalse();
    }

    @Test
    void eitherIdAloneMakesItARefund() {
        assertThat(bill().refundId(UUID.randomUUID().toString()).build().reportsRefund()).as("refused at initiation").isTrue();
        assertThat(bill().gatewayRefundId("rfnd_abc").build().reportsRefund()).as("started at the provider").isTrue();
        assertThat(deposit().refundId(UUID.randomUUID().toString()).build().reportsRefund()).isTrue();
        assertThat(deposit().gatewayRefundId("rfnd_abc").build().reportsRefund()).isTrue();
    }

    @Test
    void bothIdsTravelApartAndTheHelperStaysOffTheWire() throws Exception {
        String ours = UUID.randomUUID().toString();
        JsonNode json = mapper.valueToTree(bill().refundId(ours).gatewayRefundId("rfnd_abc").build());
        assertThat(json.get("refundId").asText()).isEqualTo(ours);
        assertThat(json.get("gatewayRefundId").asText()).isEqualTo("rfnd_abc");
        assertThat(json.has("reportsRefund")).isFalse();

        ReservationPaymentEvent back = mapper.readValue(
                mapper.writeValueAsString(deposit().refundId(ours).gatewayRefundId("rfnd_xyz").build()), ReservationPaymentEvent.class);
        assertThat(back.getRefundId()).isEqualTo(ours);
        assertThat(back.getGatewayRefundId()).isEqualTo("rfnd_xyz");
    }
}
