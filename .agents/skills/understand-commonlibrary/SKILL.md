---
name: understand-commonlibrary
description: Architectural overview and troubleshooting guide for the CommonLibrary. Use this to understand shared security configurations, outbox patterns, and event streaming principles.
---

# Understand CommonLibrary

The `CommonLibrary` prevents code duplication and strictly enforces security protocols across microservices.

## Architecture & Responsibilities

- **Security Enforcement**: The library ensures that all microservices implementing the standard security package automatically respect the `X-User-Id` and `X-User-Roles` headers propagated by the ApiGateway.
- **Inter-service Context**: By including the `FeignSecurityInterceptor`, microservices correctly forward identity state.
- **Outbox Pattern Core**: The library includes foundational classes for implementing the Transactional Outbox pattern, ensuring that event persistence and Kafka message publishing are atomic.

## Troubleshooting

- **Missing Security Context**: If a controller in a downstream service returns null for `SecurityContextHolder.getContext().getAuthentication()`, verify that the `PreAuthFilter` is actively registered in that service's filter chain.
- **Feign 401/403 Errors**: If service A calls service B and gets a 403, check if the `FeignSecurityInterceptor` is registered and propagating the headers.
- **Deserialization Errors**: If Kafka event deserialization fails, ensure both producing and consuming services use the same version of the `CommonLibrary`.
