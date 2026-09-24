package com.fooddelivery.common.client;

import com.fooddelivery.common.dto.ApiResponse;
import com.fooddelivery.common.dto.maps.RouteResponseDto;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapsServiceClientContractTest {

    /**
     * MapsIntegration answers /api/logistics/route with ApiResponse&lt;RoutePolylineDto&gt;. Declared
     * as a bare DTO, the call "worked" and returned all nulls, so nothing ever failed loudly.
     */
    @Test
    void getRouteDecodesTheApiResponseWrapper() throws Exception {
        Type type = MapsServiceClient.class.getMethod("getRoute", String.class, String.class).getGenericReturnType();
        ParameterizedType p = (ParameterizedType) type;
        assertEquals(ApiResponse.class, p.getRawType());
        assertEquals(RouteResponseDto.class, p.getActualTypeArguments()[0]);
    }
}
