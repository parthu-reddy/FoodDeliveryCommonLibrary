package com.fooddelivery.common.dto.maps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fooddelivery.common.dto.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RouteResponseDtoTest {

    /** A body as MapsIntegration's IntegrationController.getRoute sends it. */
    private static final String WIRE = """
            {"success":true,"message":"Route calculated successfully","data":{
              "polyline":"abc","distance":"4.2 km","duration":"14 mins",
              "steps":[{"duration":300,"distance":1200},{"duration":540.4,"distance":3000}]}}""";

    @Test
    void decodesTheWrappedBodyTheMapsServiceSends() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        ApiResponse<RouteResponseDto> res = mapper.readValue(WIRE, new TypeReference<>() {});
        assertEquals("abc", res.getData().getPolyline());
        assertEquals(840, res.getData().travelSeconds());
    }

    @Test
    void theOldBareDecodeLostEverything() throws Exception {
        // What getRoute did before: decode the wrapper as if it were the DTO.
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        assertNull(mapper.readValue(WIRE, RouteResponseDto.class).getPolyline());
    }

    @Test
    void fallsBackToTheReadableDuration() {
        assertEquals(840, RouteResponseDto.builder().duration("14 mins").build().travelSeconds());
        assertEquals(3900, RouteResponseDto.builder().duration("1 hr 5 mins").build().travelSeconds());
        assertEquals(720, RouteResponseDto.builder().duration("0 hours 12 minutes").build().travelSeconds());
        assertEquals(720, RouteResponseDto.builder().steps(List.of(Map.of("distance", 5))).duration("12 min").build().travelSeconds());
    }

    @Test
    void nullWhenThereIsNothingToRead() {
        assertNull(RouteResponseDto.builder().build().travelSeconds());
        assertNull(RouteResponseDto.builder().duration("soon").build().travelSeconds());
    }
}
