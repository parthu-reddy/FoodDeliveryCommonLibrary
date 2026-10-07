package com.fooddelivery.common.dto.maps;

/** Atomic, internal order location snapshot. Never returned by a customer endpoint. */
public record LiveRiderLocation(String driverId, String orderId, String cityId,
                                double latitude, double longitude, long observedAtEpochMs) {
    public static final long MAX_AGE_MS = 30_000;
    public static String redisKey(String orderId) { return "tracking:order-location:" + orderId; }

    public boolean isFresh(long nowMs) {
        return observedAtEpochMs <= nowMs && nowMs - observedAtEpochMs < MAX_AGE_MS
                && Double.isFinite(latitude) && latitude >= -90 && latitude <= 90
                && Double.isFinite(longitude) && longitude >= -180 && longitude <= 180;
    }
}
