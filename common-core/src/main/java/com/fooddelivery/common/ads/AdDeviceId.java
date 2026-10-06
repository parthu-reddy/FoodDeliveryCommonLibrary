package com.fooddelivery.common.ads;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * The device id ad serving and ad tracking both use for a signed-in customer.
 *
 * <p>CustomerApplication sends it to BiddingEngine with every ad request, and UserTrackingService keys
 * impressions, deduplication and the frequency cap ({@code ad:cap:{deviceId}:{campaignId}}, read back by
 * BiddingEngine) on it. Both must derive the same value from the same customer, or the cap counts one
 * id and checks another. Stable per customer; not reversible to the customer id in either service's logs.
 */
public final class AdDeviceId {

    private AdDeviceId() { }

    public static String of(String customerId) {
        return UUID.nameUUIDFromBytes(("ad-device:" + customerId).getBytes(StandardCharsets.UTF_8)).toString();
    }
}
