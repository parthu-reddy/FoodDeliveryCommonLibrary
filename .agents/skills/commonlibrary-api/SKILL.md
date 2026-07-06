---
name: commonlibrary-api
description: API reference and integration guide for the CommonLibrary. Use this to understand shared events, constants, and data transfer objects used across microservices.
---

# CommonLibrary API Reference

The `CommonLibrary` is not an executable microservice, so it does not expose REST APIs. Instead, it exposes Java classes, Interfaces, and standard Event Schemas for internal use.

## Core Packages

1. **`com.fooddelivery.common.dto`**:
   - `ApiResponse<T>`: Standard JSON response wrapper. All controllers should wrap their responses in this format.

2. **`com.fooddelivery.common.event`**:
   - Contains schemas for Kafka event payloads (e.g., `PaymentEvent`, `OrderEvent`).

3. **`com.fooddelivery.common.security`**:
   - `PreAuthFilter`: Extracts headers and builds the Spring `SecurityContextHolder`.
   - `FeignSecurityInterceptor`: Propagates security context onto outgoing Feign calls.
