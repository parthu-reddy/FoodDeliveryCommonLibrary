package com.fooddelivery.common.test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Scrapes {@code /actuator/prometheus} the way Prometheus or an operator on the private network does: a plain GET to the
 * service's own port, no identity headers, through every servlet filter and the service's security chain.
 *
 * <p>Written 2026-10-17 (PrometheusRegistry_2026-10-17). Five services answered 404 on Dev because the registry only
 * arrived through common-messaging, and a test that merely found the endpoint bean could not see a security chain
 * refusing the scrape (chat-service's did). A missing registry answers 404, a refusing chain 401/403; the failure
 * message names the status, which the gate reads.
 *
 * <p>The sample checked is {@code jvm_memory_used_bytes}: any registry-backed scrape has it, and a 200 without it would
 * mean something other than the Prometheus endpoint answered (a mocked filter, for one, ends the request empty).
 */
public final class PrometheusScrape {

    private PrometheusScrape() {
    }

    public static void assertServedAnonymously(int port) throws Exception {
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/actuator/prometheus"))
                        .timeout(Duration.ofSeconds(30)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        String head = response.body().length() > 300 ? response.body().substring(0, 300) : response.body();
        assertEquals(200, response.statusCode(),
                () -> "scrape of /actuator/prometheus answered " + response.statusCode() + ": " + head);
        assertTrue(response.body().contains("jvm_memory_used_bytes"),
                () -> "scrape of /actuator/prometheus answered 200 without a jvm_memory_used_bytes sample: " + head);
    }
}
