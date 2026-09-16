# CommonLibrary

The `CommonLibrary` is a foundational dependency for the Food Delivery microservices ecosystem. It provides shared resources to reduce code duplication and enforce consistency across all applications.

## Key Features
- **Shared DTOs**: Centralized data transfer objects (e.g., `ApiResponse`, `OrderEvent`).
- **Security Context**: Provides `PreAuthFilter` for extracting `X-User-Id` and `X-User-Roles` headers from incoming ApiGateway requests.
- **Feign Interceptors**: Provides `FeignSecurityInterceptor` for propagating security contexts and identifying calling services on outgoing inter-service requests.
- **Event Contracts**: Standardized Kafka event schemas.
- **Outbox Pattern**: Common implementation for transactional outbox pattern messaging.

## Setup & Build
This library is not a runnable application. It must be installed into your local Maven repository or shared artifact repository.

```bash
mvn clean install
```

Downstream services include it via:
```xml
<dependency>
    <groupId>com.fooddelivery</groupId>
    <artifactId>common-library</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
 
