package com.fooddelivery.common.dto.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fooddelivery.common.enums.DeliveryStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Everything a reviewing service needs to decide whether a review is allowed, in one payload.
 *
 * <p>The question review eligibility actually turns on is not "does this restaurant exist" but "was
 * this restaurant on this customer's delivered order". Only the order's owner can answer that, so
 * it answers it once rather than the caller reassembling it from existence checks — which was the
 * previous design, and which could not succeed: {@code InternalRestaurantController} requires
 * {@code SERVICE}, {@code RESTAURANT} or {@code ADMIN}, while the propagated identity on a review
 * request is the customer's.
 *
 * <p>Completion is read from {@link #deliveryStatus}, never from {@code OrderStatus}: that enum has
 * no {@code DELIVERED} value by design — {@code HANDED_OVER} is its last non-terminal value and the
 * food has not arrived at that point.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReviewContextDto {

    private UUID orderId;

    private UUID customerId;

    /** Snapshotted by the reviewing service into the review's author label; never looked up on read. */
    private String customerName;

    /** An OUTLET id. Comparing it to a user id never matches — see {@code OrderSecurityHelper}. */
    private UUID restaurantId;

    private String restaurantName;

    /**
     * Null when no driver was ever assigned, in which case the order has no reviewable driver.
     *
     * <p>No name accompanies it: the customer-side {@code orders} table has no rider-name column
     * (only {@code delivery_executive_id}), and {@code OrderMapper} has never populated
     * {@code OrderResponse.deliveryExecutiveName}. Carrying an always-null field here would make the
     * contract claim something it cannot deliver; the rating sheet labels this target
     * "Delivery partner" instead.
     */
    private UUID deliveryExecutiveId;

    private DeliveryStatus deliveryStatus;

    /** Null until the order is delivered. Start of the review window. */
    private LocalDateTime deliveredAt;

    private List<OrderReviewItemDto> items;
}
