package com.fooddelivery.common.event;

import com.fooddelivery.common.enums.FulfilmentType;

import java.util.Map;

/**
 * An order event whose shape depends on how the order is fulfilled: DELIVERY carries the rider's dispatch scope and no
 * table, DINE_IN carries a table label and none of the dispatch scope. {@link FulfilmentShape} enforces it at binding,
 * so a consumer branches on {@link #getFulfilmentType()} and never on whether a dispatch field happens to be null.
 */
public interface FulfilmentScopedEvent {

    FulfilmentType getFulfilmentType();

    String getTableLabel();

    /**
     * The fields only a delivery has, by wire name, in declaration order: every one required for DELIVERY, every one
     * absent for DINE_IN. Not named as a getter, so Jackson does not add it to the wire shape.
     */
    Map<String, Object> deliveryScope();
}
