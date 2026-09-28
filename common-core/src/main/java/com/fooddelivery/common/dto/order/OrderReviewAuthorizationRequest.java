package com.fooddelivery.common.dto.order;

import java.util.List;
import java.util.UUID;

import com.fooddelivery.common.enums.RoleName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A trusted review-service assertion about the authenticated actor plus exact requested targets.
 * The order service verifies every field against its own order data before allowing a review.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReviewAuthorizationRequest {

    @NotNull
    private UUID reviewerId;

    @NotNull
    private RoleName reviewerRole;

    @NotEmpty
    @Size(max = 100)
    @Valid
    private List<OrderReviewTargetAuthorizationRequest> targets;
}
