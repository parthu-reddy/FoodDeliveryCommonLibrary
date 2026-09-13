package com.fooddelivery.common.idempotency;

import com.fooddelivery.common.repository.IIdempotencyKeyRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IdempotencyKeySweeperTest {

    @SuppressWarnings("unchecked")
    private static ObjectProvider<IIdempotencyKeyRepository> providerOf(IIdempotencyKeyRepository repo) {
        ObjectProvider<IIdempotencyKeyRepository> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(repo);
        return provider;
    }

    @Test
    void deletesKeysOlderThanTheRetentionWindow() {
        IIdempotencyKeyRepository repo = mock(IIdempotencyKeyRepository.class);
        when(repo.deleteOlderThan(any())).thenReturn(3);

        new IdempotencyKeySweeper(providerOf(repo), 7).sweepExpiredKeys();

        ArgumentCaptor<LocalDateTime> cutoff = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repo).deleteOlderThan(cutoff.capture());
        // Measured against a `now` taken AFTER the call: the sweeper's own now() is necessarily
        // later than one captured before it, so counting from the earlier instant truncates to 6.
        long days = ChronoUnit.DAYS.between(cutoff.getValue(), LocalDateTime.now());
        assertThat(days)
                .as("cutoff must be the retention window in the past, not now or the future")
                .isEqualTo(7);
    }

    @Test
    void doesNothingWhenTheServiceHasNoIdempotencyRepository() {
        new IdempotencyKeySweeper(providerOf(null), 7).sweepExpiredKeys();   // must not throw
    }

    @Test
    void aFailingDeleteDoesNotEscapeAndKillTheScheduler() {
        IIdempotencyKeyRepository repo = mock(IIdempotencyKeyRepository.class);
        when(repo.deleteOlderThan(any())).thenThrow(new RuntimeException("db down"));

        new IdempotencyKeySweeper(providerOf(repo), 7).sweepExpiredKeys();   // must not throw

        verify(repo).deleteOlderThan(any());
    }
}
