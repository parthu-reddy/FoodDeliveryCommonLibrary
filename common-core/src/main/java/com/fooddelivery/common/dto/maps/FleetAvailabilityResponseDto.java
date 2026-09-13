package com.fooddelivery.common.dto.maps;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FleetAvailabilityResponseDto {
    private Boolean available;
}
