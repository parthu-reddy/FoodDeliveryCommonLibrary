package com.fooddelivery.common.dto.maps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MapsIntegration's {@code GET /api/logistics/route} body, inside its {@code ApiResponse}.
 *
 * <p>The shape follows what that service sends ({@code RoutePolylineDto}): {@code distance} and
 * {@code duration} are Ola's READABLE strings ("12 mins"), and {@code steps} is Ola's step list
 * passed through, each step carrying a numeric {@code duration} in seconds. This class used to
 * declare both as Double and be decoded from the unwrapped body, so every field came back null --
 * see {@link com.fooddelivery.common.client.MapsServiceClient#getRoute}.
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
    private List<Map<String, Object>> steps;

    private static final Pattern HOURS = Pattern.compile("(\\d+)\\s*(?:h|hr|hrs|hour|hours)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern MINUTES = Pattern.compile("(\\d+)\\s*(?:m|min|mins|minute|minutes)\\b", Pattern.CASE_INSENSITIVE);

    /**
     * Driving time in whole seconds, or null when the route carries none usable.
     *
     * <p>The sum of the steps' numeric durations; if no step has one, the readable duration parsed
     * for hours and minutes. Never a guess: a string it cannot read gives null.
     */
    public Integer travelSeconds() {
        if (steps != null) {
            double sum = 0;
            boolean any = false;
            for (Map<String, Object> step : steps) {
                Object d = step == null ? null : step.get("duration");
                if (d instanceof Number n && n.doubleValue() >= 0) {
                    sum += n.doubleValue();
                    any = true;
                }
            }
            if (any) return (int) Math.round(sum);
        }
        if (duration == null || duration.isBlank()) return null;
        Matcher h = HOURS.matcher(duration);
        Matcher m = MINUTES.matcher(duration);
        boolean hasH = h.find();
        boolean hasM = m.find();
        if (!hasH && !hasM) return null;
        int seconds = (hasH ? Integer.parseInt(h.group(1)) * 3600 : 0) + (hasM ? Integer.parseInt(m.group(1)) * 60 : 0);
        return seconds;
    }
}
