package com.fooddelivery.common.client;

import com.fooddelivery.common.exception.ResourceNotFoundException;
import feign.FeignException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/** A 404 from the restaurant service is "no such outlet", not "the service is down" (BusinessPlatform A2). */
class RestaurantServiceClientFallbackTest {

    private final RestaurantServiceClientFallback factory = new RestaurantServiceClientFallback();

    @Test
    void aMissingOutletOrBrandIsNotFound() {
        RestaurantServiceClient client = factory.create(mock(FeignException.NotFound.class));
        assertThatThrownBy(() -> client.getOutletOrganisation(UUID.randomUUID())).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> client.getBrandOrganisation(UUID.randomUUID())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void anythingElseIsUnavailableNeverAnAnswer() {
        for (Throwable cause : new Throwable[] {
                mock(FeignException.ServiceUnavailable.class), mock(FeignException.InternalServerError.class),
                new java.net.SocketTimeoutException("read timed out"), new RuntimeException("circuit open")}) {
            RestaurantServiceClient client = factory.create(cause);
            assertThatThrownBy(() -> client.getOutletOrganisation(UUID.randomUUID())).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> client.getBrandOrganisation(UUID.randomUUID())).isInstanceOf(IllegalStateException.class);
        }
    }
}
