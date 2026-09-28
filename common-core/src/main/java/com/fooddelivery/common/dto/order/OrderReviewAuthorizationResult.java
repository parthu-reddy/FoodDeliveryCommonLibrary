package com.fooddelivery.common.dto.order;

import com.fooddelivery.common.enums.ReviewEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Authorization result for one exact review target; never means that a different target is allowed. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReviewAuthorizationResult {

    private ReviewEntityType targetType;
    private String targetId;
    private boolean allowed;
    private String reasonCode;
}
