package com.fooddelivery.common.dto.order;

import com.fooddelivery.common.enums.ReviewEntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One exact target whose relationship to an order must be checked by the order service. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReviewTargetAuthorizationRequest {

    @NotNull
    private ReviewEntityType targetType;

    @NotBlank
    private String targetId;
}
