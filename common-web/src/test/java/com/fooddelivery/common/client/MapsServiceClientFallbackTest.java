package com.fooddelivery.common.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapsServiceClientFallbackTest {

    @Test
    void fleetAvailabilityFailsClosedWhenMapsIsUnavailable() {
        MapsServiceClientFallback fallback = new MapsServiceClientFallback();

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> fallback.checkFleetAvailability("BLR", 12.97, 77.59, 5.0));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, error.getStatusCode());
    }
}
