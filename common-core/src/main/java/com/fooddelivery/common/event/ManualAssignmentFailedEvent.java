package com.fooddelivery.common.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The terminal result of one audited manual rider-assignment operation that Delivery rejected
 * before it changed any dispatch state.
 *
 * <p>The reason is a stable, operator-safe code rather than an implementation exception. The
 * operation ID lets CustomerApplication ignore a result for an intervention that an administrator
 * has already superseded.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ManualAssignmentFailedEvent implements OrderScopedEvent {

    @NotNull(message = "orderId is required")
    private String orderId;

    @NotBlank(message = "operationId is required")
    private String operationId;

    @NotBlank(message = "actorId is required")
    private String actorId;

    @NotBlank(message = "driverId is required")
    private String driverId;

    @NotBlank(message = "reasonCode is required")
    private String reasonCode;

    private Long timestamp;

    @Override
    public java.util.UUID orderUuid() {
        return orderId == null || orderId.isBlank() ? null : java.util.UUID.fromString(orderId);
    }
}
