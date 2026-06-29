# Common Library

`CommonLibrary` is a foundational Spring Boot library created to handle shared business logic, domain events, cross-cutting concerns, and configurations across the Food Delivery microservices. 

It prevents code duplication and enforces strict contracts between services.

## Core Components

1. **Domain Events (`com.fooddelivery.common.event`)**
   - Standardizes the JSON schema of Kafka events.
   - Events include: `OrderCreatedEvent`, `OrderPaidEvent`, `NotificationRequestEvent`, `OutboxEvent`.
   - By importing this library, all microservices serialize and deserialize these events identically.

2. **DTOs and standard responses (`com.fooddelivery.common.dto`)**
   - `ApiResponse<T>`: Wraps all API responses in a unified `{ success, data, message, timestamp }` format.

3. **Global Exception Handling (`com.fooddelivery.common.exception`)**
   - `@ControllerAdvice` hooks into all REST controllers to map exceptions to standard `ApiResponse` objects with appropriate HTTP status codes (e.g. `ResourceNotFoundException`, `OrderProcessingException`).

4. **Security & Validation Filters (`com.fooddelivery.common.filter`)**
   - `IdempotencyFilter`: Ensures POST/PUT requests with an `Idempotency-Key` header aren't executed multiple times (backed by Redis or concurrent maps).
   - `RequestCachingFilter`: Wraps `HttpServletRequest` into `CachedBodyHttpServletRequest`, enabling multiple reads of the request body (vital for Webhook HMAC validation in Payment gateways).

5. **Kafka Constants (`com.fooddelivery.common.constants.KafkaConstants`)**
   - Maintains topic names (`order-events`, `payment-events`, `notification-events`) to avoid typos.

## Usage

To use this library in another microservice, first install it to your local Maven repository:

```bash
cd CommonLibrary
mvn clean install
```

Then, add the dependency to your microservice's `pom.xml`:

```xml
<dependency>
    <groupId>com.fooddelivery</groupId>
    <artifactId>common-library</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Finally, if using the `@ControllerAdvice` or Filters, ensure your Spring Boot application's `@ComponentScan` covers the `com.fooddelivery.common` base package:

```java
@SpringBootApplication(scanBasePackages = {"com.fooddelivery.payments", "com.fooddelivery.common"})
```
