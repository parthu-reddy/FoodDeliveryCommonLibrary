package com.fooddelivery.common.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One order line as the restaurant must cook it, carried on ORDER_PAID.
 *
 * <p>Snapshot taken when the quote was priced and copied onto the order line at checkout; consumers never re-read the
 * catalog for it, so a later menu or category change cannot alter what a paid order asks the kitchen to cook.
 * {@code orderItemId} is order-service's {@code order_items.id} and is the kitchen's item id. {@code price} is the unit
 * price, the same meaning as {@code order_items.price}. {@code categoryId}/{@code categoryName} are null when the menu
 * item had no category; {@code veg} is null when the catalog did not say.
 */
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public record OrderLineSnapshot(
        @NotNull UUID orderItemId,
        @NotNull UUID menuItemId,
        @NotBlank String name,
        @NotNull @Positive Integer quantity,
        @NotNull @PositiveOrZero BigDecimal price,
        UUID categoryId,
        String categoryName,
        Boolean veg) {
}
