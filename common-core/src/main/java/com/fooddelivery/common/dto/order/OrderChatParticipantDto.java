package com.fooddelivery.common.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * A participant identity derived from the order aggregate for chat authorization.
 *
 * <p>The restaurant entry is an outlet id rather than an identity-user id. The chat service
 * resolves its owner through RestaurantService before persisting a session participant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderChatParticipantDto {

    @NotNull
    private UUID id;

    /** CUSTOMER, RESTAURANT_OUTLET, or DELIVERY. */
    @NotBlank
    private String participantType;

    @NotBlank
    private String displayName;
}
