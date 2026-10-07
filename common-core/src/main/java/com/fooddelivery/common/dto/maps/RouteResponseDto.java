package com.fooddelivery.common.dto.maps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MapsIntegration's {@code GET /api/logistics/route} body, inside its {@code ApiResponse}.
 *
 * <p>The shape follows what that service sends ({@code RoutePolylineDto}). {@code durationSeconds}
 * and {@code distanceMeters} are the provider's numeric route totals and are the values to compute
 * with; {@code distance} and {@code duration} are Ola's readable labels ("0.79", "0 hours 3 minutes"),
 * for display only. A total the provider did not send is null -- never estimated from the labels.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteResponseDto {
    private String polyline;
    private String distance;
    private String duration;
    /** Provider route totals, in SI units; independent of the readable labels. */
    private Integer durationSeconds;
    private Integer distanceMeters;
    private List<Map<String, Object>> steps;
}
