package com.fooddelivery.common.repository;

import com.fooddelivery.common.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

@Repository
public interface IIdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
    
    @Modifying
    @Query(value = "INSERT INTO idempotency_keys (idempotency_key, created_at) VALUES (:k, now()) ON CONFLICT DO NOTHING", nativeQuery = true)
    int tryClaim(@Param("k") String key);
}
