package com.fooddelivery.common.repository;

import com.fooddelivery.common.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IIdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
}
