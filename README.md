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

## Maps Integration Contracts

The `common-web` and `common-core` modules provide the declarative Feign client and standardized DTOs for the Maps integration service:
- **`MapsServiceClient`**: Declares synchronous inter-service contracts for:
  - `GET /api/v1/internal/fleet/availability/check`: Fleet availability checking by geographic circle (`cityId`, `lat`, `lng`, `radius`).
  - `GET /api/places/autocomplete`: Place autocomplete query returning `List<PlaceAutocompleteDto>`.
  - `GET /api/places/reverse-geocode`: Reverse geocoding coordinates into `ReverseGeocodeDto` (`address`).
  - `GET /api/logistics/distance`: Distance query returning `DistanceResponseDto`.
  - `GET /api/logistics/route`: Route polyline and metrics calculation wrapped in `ApiResponse<RouteResponseDto>`, decoding SI units (`durationSeconds`, `distanceMeters`) alongside readable display strings.
  - `POST /api/v1/internal/fleet/release`: Restores driver availability after dispatch completion.
  - `POST /api/v1/internal/fleet/availability`: Sets driver availability in either direction, honouring `request.available`.

### Financial Integrity & Strict Fallback Policy
`MapsServiceClientFallback` enforces strict financial integrity:
- **Fail Fast & No Fallbacks**: On failure or timeout of `getDistance()`, it throws `IllegalStateException` immediately. Never falls back to default distances or estimated delivery fees.
- **Fail Closed for Fleet Availability**: On failure of `checkFleetAvailability()`, it logs an error and throws `ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE)` rather than failing open.
- **Route Unavailable**: Throws `ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE)` when routing engine is unavailable.

