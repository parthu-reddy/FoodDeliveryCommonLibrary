package com.fooddelivery.common.service;

import com.fooddelivery.common.constants.RedisKeyConstants;
import com.fooddelivery.common.exception.ApplicationRateLimitException;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.UUID;

/** Constructed only by the application services that own these writes. */
@RequiredArgsConstructor
public class ApplicationRateLimits {
    private final RateLimitingService limits;

    public void organisationWrite(UUID organisationId) {
        consume("restaurant:" + organisationId);
    }

    public void deliveryWrite(UUID applicantId) {
        consume("delivery:" + applicantId);
    }

    private void consume(String suffix) {
        var probe =
                limits.resolveBucket(
                                RedisKeyConstants.RL_APP_WRITE + suffix,
                                30,
                                30,
                                Duration.ofHours(1))
                        .tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            throw new ApplicationRateLimitException(
                    Math.max(1, (probe.getNanosToWaitForRefill() + 999_999_999L) / 1_000_000_000L));
        }
    }
}
