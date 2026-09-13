package com.fooddelivery.common.dto.order;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One line of an order, as far as reviewing is concerned.
 *
 * <p>{@code menuItemId} is a {@code MasterMenuItem} id, not an outlet-level one:
 * {@code CatalogService} builds every {@code MenuItemDTO.id} from {@code master.getId()}, and that
 * is the id the order carries. A review of {@code EntityType.PRODUCT} is therefore a review of the
 * brand's dish, not of one outlet's copy of it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReviewItemDto {

    private UUID menuItemId;

    /** The name as it was at order time. Used to label the target in the rating sheet. */
    private String name;
}
