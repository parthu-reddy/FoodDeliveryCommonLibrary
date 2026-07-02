---
name: common-library-guide
description: Comprehensive guide to the CommonLibrary shared dependency. Use this to understand shared DTOs, domain events, enums, constants, Kafka topics, cross-cutting filters, the Outbox pattern module, and testing infrastructure used across all Food Delivery microservices.
---

# CommonLibrary: Shared Contracts & Cross-Cutting Concerns

The `CommonLibrary` is a Maven dependency (`com.fooddelivery:common-library`) that must be installed to the local `.m2` repository (`mvn clean install`) before building any microservice. Every service in the platform depends on it.

## Package Structure

All classes are under `com.fooddelivery.common`.

### 1. Constants

#### `EventType` — Canonical Event Type Strings
Centralizes all Kafka event type identifiers. **Always use these constants** instead of raw strings to prevent typos.

| Category | Constants |
|---|---|
| **Order Lifecycle** | `ORDER_CREATED`, `ORDER_PAID`, `ORDER_ACCEPTED`, `ORDER_READY`, `ORDER_DELIVERED`, `ORDER_REJECTED`, `ORDER_STATUS_UPDATED` |
| **Cancellation** | `ORDER_CANCELLED`, `ORDER_CANCELLED_BY_RESTAURANT` |
| **Delay Approval** | `ORDER_DELAY_APPROVAL_REQUESTED`, `ORDER_DELAY_APPROVED`, `ORDER_DELAY_REJECTED` |
| **Dispatch** | `DISPATCH_CANDIDATE_FOUND`, `DISPATCH_FAILED`, `DRIVER_ASSIGNED`, `ORDER_DRIVER_REJECTED` |
| **Delivery** | `DELIVERY_FAILED` |
| **Notification** | `NOTIFICATION_REQUEST` |
| **Notification Templates** | `NOTIFY_DRIVER_ON_THE_WAY`, `NOTIFY_DELAY_APPROVAL_REQUESTED`, `NOTIFY_ORDER_READY_FOR_PICKUP`, `NOTIFY_ORDER_CANCELLED_DELAY_TIMEOUT` |
| **Payment** | `PAYMENT_WEBHOOK`, `PAYMENT_COMPLETED` (`PaymentCompletedEvent`), `PAYMENT_FAILED` (`PaymentFailedEvent`), `PAYMENT_REFUNDED` (`PaymentRefundedEvent`) |

#### `KafkaConstants` — Topic & Consumer Group Names

| Constant | Value |
|---|---|
| `TOPIC_ORDER_EVENTS` | `order-events` |
| `TOPIC_PAYMENT_EVENTS` | `payment-events` |
| `TOPIC_NOTIFICATIONS_DISPATCH` | `platform.notifications.dispatch` |
| `TOPIC_NOTIFICATIONS_DLQ` | `notifications-dlq` |
| `TOPIC_LOGISTICS_DISPATCH` | `platform.logistics.dispatch` |
| `GROUP_FOOD_DELIVERY` | `food-delivery-group` |
| `GROUP_RESTAURANT_SERVICE` | `restaurant-service-group` |
| `GROUP_DELIVERY_SERVICE` | `delivery-service-group` |
| `GROUP_NOTIFICATION_SERVICE` | `notification-service-group` |
| `GROUP_MAPS_INTEGRATION` | `maps-integration-group` |

#### `PaymentIntentStatus` — Payment lifecycle statuses.

### 2. Enums

#### `OrderStatus` — The canonical order state machine
```
CREATED → PAID → AWAITING_DELAY_APPROVAL → ACCEPTED → READY_FOR_PICKUP → DISPATCHED → OUT_FOR_DELIVERY → DELIVERED
                                         ↘ CANCELLED (delay rejected)
                    ↘ CANCELLED_BY_RESTAURANT (rejected/cancelled by restaurant)
                                                                      ↘ DELIVERY_FAILED (no drivers)
                                                                                          ↘ PARTIALLY_REFUNDED
                                                                                          ↘ CANCELLED_AND_REFUNDED
```
**Important**: Ordinal-based forward-only transitions are enforced by `OrderSagaOrchestrator`. Backward transitions are blocked.

#### `ChannelType` — Notification channels: `PUSH`, `SMS`, `WHATSAPP`, `EMAIL`.

### 3. Domain Events (DTOs)

| Event Class | Key Fields | Published To |
|---|---|---|
| `OrderCreatedEvent` | orderId, customerId, restaurantId, totalAmount | `order-events` |
| `OrderPaidEvent` | orderId, restaurantId, deliveryLat, deliveryLng, estimatedPrepTime | `order-events` |
| `OutboxEvent` | eventId, eventType, payload, aggregateId | (Outbox table) |
| `PaymentSucceededEvent` | orderId, paymentIntentId, amount, gateway | `payment-events` |
| `PaymentFailedEvent` | orderId, reason | `payment-events` |
| `PaymentRefundedEvent` | orderId, refundId, amount | `payment-events` |
| `NotificationRequestEvent` | eventId, userId, eventName, channel, explicitRecipient, templateParams, payload | `platform.notifications.dispatch` |

### 4. Cross-Cutting Filters

#### `IdempotencyFilter`
- Requires `Idempotency-Key` HTTP header on POST/PUT requests.
- Uses Redis (`setIfAbsent`) to enforce distributed idempotency with TTL-based lock expiry.

#### `RequestCachingFilter`
- Wraps `HttpServletRequest` in `ContentCachingRequestWrapper`.
- Enables reading the raw request body multiple times (critical for HMAC signature verification in payment webhooks).

### 5. Exception Handling

- **`GlobalExceptionHandler`** — `@ControllerAdvice` returning standardized `ApiResponse<T>` error bodies.
- **`ResourceNotFoundException`** — 404 responses.
- **`OrderProcessingException`** — 422 responses for business logic violations.

### 6. Outbox Pattern Module (`com.fooddelivery.common.outbox`)

Enables the **Transactional Outbox Pattern** for any microservice.

- **`@EnableOutbox`** — Annotation to activate the module in a service's `@Configuration`.
- **`OutboxConfiguration`** — Auto-configures the `OutboxEventPoller` and `OutboxEventRepository`.
- **`OutboxEventEntity`** — JPA entity mapped to the `outbox_events` table (columns: `id`, `event_type`, `payload`, `aggregate_id`, `status`, `created_at`).
- **`OutboxEventRepository`** — Spring Data JPA repository with a query to find `UNPROCESSED` events.
- **`OutboxEventPoller`** — `@Scheduled` background task that polls `outbox_events`, publishes to Kafka, and marks rows as `PROCESSED`.

**Requirement**: Any service using `@EnableOutbox` must have an `outbox_events` table in its database (typically created via Flyway migration).

### 7. Shared Services

- **`NotificationRouterService`** — Helper to construct and publish `NotificationRequestEvent` to the Outbox for asynchronous notification dispatch.
- **`RateLimitingService`** — Distributed rate limiter using Bucket4j + Redis.

### 8. Standard API Response Wrapper

All REST responses use `ApiResponse<T>`:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2024-05-20T10:15:30"
}
```

### 9. Testing Infrastructure

- **`BaseIntegrationTest`** — Abstract test class providing Testcontainers-based PostgreSQL and Kafka setup for integration tests. All service integration tests should extend this.

## Building

```bash
cd CommonLibrary
mvn clean install
```
This must be done **before** building any other microservice.
