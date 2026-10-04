package com.fooddelivery.common.service;

import com.fooddelivery.common.constants.RedisKeyConstants;
import com.fooddelivery.common.exception.ApplicationRateLimitException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationRateLimitsTest {
    @Test void organisationWritesShareOneBucketAndOtherOrganisationsDoNotExhaustIt() {
        var limits = mock(RateLimitingService.class);
        UUID organisation = UUID.randomUUID(), other = UUID.randomUUID();
        var exhausted = bucket();
        when(limits.resolveBucket(RedisKeyConstants.RL_APP_WRITE + "restaurant:" + organisation,
                30, 30, Duration.ofHours(1))).thenReturn(exhausted);
        when(limits.resolveBucket(RedisKeyConstants.RL_APP_WRITE + "restaurant:" + other,
                30, 30, Duration.ofHours(1))).thenReturn(bucket());
        var writer = new ApplicationRateLimits(limits);
        for (int i = 0; i < 30; i++) writer.organisationWrite(organisation);
        var error = catchThrowableOfType(() -> writer.organisationWrite(organisation), ApplicationRateLimitException.class);
        assertThat(error.retryAfter()).isBetween(3598L, 3600L);
        assertThatCode(() -> writer.organisationWrite(other)).doesNotThrowAnyException();
    }

    @Test void deliveryAndRestaurantWritesUseDistinctKeysForTheSameUuid() {
        var limits = mock(RateLimitingService.class);
        UUID applicant = UUID.randomUUID();
        when(limits.resolveBucket(RedisKeyConstants.RL_APP_WRITE + "delivery:" + applicant,
                30, 30, Duration.ofHours(1))).thenReturn(bucket());
        var writer = new ApplicationRateLimits(limits);
        writer.deliveryWrite(applicant);
        verify(limits).resolveBucket(RedisKeyConstants.RL_APP_WRITE + "delivery:" + applicant,
                30, 30, Duration.ofHours(1));
        verifyNoMoreInteractions(limits);
    }

    private static Bucket bucket() {
        return Bucket.builder().addLimit(Bandwidth.classic(30, Refill.intervally(30, Duration.ofHours(1)))).build();
    }
}
