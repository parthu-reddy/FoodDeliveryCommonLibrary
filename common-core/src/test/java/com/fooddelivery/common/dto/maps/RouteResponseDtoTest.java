package com.fooddelivery.common.dto.maps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fooddelivery.common.dto.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RouteResponseDtoTest {

    /** A body as MapsIntegration's IntegrationController.getRoute sends it (live values, 2026-10-07). */
    private static final String WIRE = """
            {"success":true,"message":"Route calculated successfully","data":{
              "polyline":"abc","distance":"4.58","duration":"0 hours 16 minutes",
              "durationSeconds":939,"distanceMeters":4577,
              "steps":[{"duration":300,"distance":1200},{"duration":639,"distance":3377}]}}""";

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void decodesTheProvidersNumericTotals() throws Exception {
        ApiResponse<RouteResponseDto> res = mapper.readValue(WIRE, new TypeReference<>() {});
        assertEquals("abc", res.getData().getPolyline());
        assertEquals(939, res.getData().getDurationSeconds());
        assertEquals(4577, res.getData().getDistanceMeters());
    }

    @Test
    void aTotalTheProviderDidNotSendStaysNull_notEstimatedFromTheLabel() throws Exception {
        ApiResponse<RouteResponseDto> res = mapper.readValue("""
                {"success":true,"message":"ok","data":{"polyline":"abc","duration":"0 hours 16 minutes"}}""",
                new TypeReference<>() {});
        assertNull(res.getData().getDurationSeconds());
        assertNull(res.getData().getDistanceMeters());
    }

    @Test
    void theOldBareDecodeLostEverything() throws Exception {
        // What getRoute did before: decode the wrapper as if it were the DTO.
        ObjectMapper lenient = new ObjectMapper().registerModule(new JavaTimeModule())
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        assertNull(lenient.readValue(WIRE, RouteResponseDto.class).getPolyline());
    }
}
