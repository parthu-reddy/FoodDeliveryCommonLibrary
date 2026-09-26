package com.fooddelivery.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "idempotency_keys")
@lombok.Getter
@lombok.Setter
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@lombok.Builder
public class IdempotencyKey {

    @Id
    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    // The class is @Builder but nothing uses IdempotencyKey.builder() today; construction goes
    // through the explicit constructor, where field initializers do run. Without
    // @Builder.Default the first builder use would set createdAt null against a nullable=false
    // column and fail at insert. Cheap to prevent, awkward to diagnose.
    @lombok.Builder.Default
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public IdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
        this.createdAt = Instant.now();
    }
}
