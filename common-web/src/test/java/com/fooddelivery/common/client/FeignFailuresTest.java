package com.fooddelivery.common.client;

import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

class FeignFailuresTest {

    private static FeignException status(int code) {
        Request request = Request.create(Request.HttpMethod.GET, "http://x/y", Map.of(), null, StandardCharsets.UTF_8, null);
        return FeignException.errorStatus("C#m()", feign.Response.builder().status(code).reason("r").request(request)
                .headers(Map.of()).build());
    }

    @Test
    void aNotFoundIsSeenBareAndThroughTheTimeLimitersExecutionException() {
        assertThat(FeignFailures.notFound(status(404))).isTrue();
        // What a fallback factory and the circuit breaker actually receive with the Feign circuit breaker on (F-8).
        assertThat(FeignFailures.notFound(new ExecutionException(status(404)))).isTrue();
        assertThat(new FeignFailures.NotFoundIsAnAnswer().test(new ExecutionException(status(404)))).isTrue();
    }

    @Test
    void everythingElseIsAFailure() {
        assertThat(FeignFailures.notFound(status(503))).isFalse();
        assertThat(FeignFailures.notFound(new ExecutionException(status(500)))).isFalse();
        assertThat(FeignFailures.notFound(new IllegalStateException("boom"))).isFalse();
        assertThat(FeignFailures.notFound(null)).isFalse();
        assertThat(new FeignFailures.NotFoundIsAnAnswer().test(new java.util.concurrent.TimeoutException())).isFalse();
    }
}
