# CommonLibrary Architecture

The `CommonLibrary` provides shared DTOs, configurations, enums, security filters, and events that are utilized across the Food Delivery microservices ecosystem. It ensures code reusability and standardizes communication models between services (e.g., via Kafka and Feign).

## Detailed Sequence Diagram

Since `CommonLibrary` is not a standalone service but a shared dependency, the flow diagram illustrates how it integrates into the request lifecycle of a typical microservice.

```mermaid
sequenceDiagram
    participant DownstreamService as Downstream Service
    participant CommonLibrary
    participant TargetService as Target Service

    %% Security & Feign Flow
    rect rgb(240, 248, 255)
        note right of DownstreamService: Internal Communication Flow
        DownstreamService->>CommonLibrary: FeignSecurityInterceptor (Intercept outgoing call)
        CommonLibrary->>CommonLibrary: Extract X-User-Id, X-User-Roles from SecurityContext
        CommonLibrary->>TargetService: Forward request with injected headers
    end

    %% Ingress Security Flow
    rect rgb(245, 245, 245)
        note right of TargetService: Request Ingress Flow
        TargetService->>CommonLibrary: PreAuthFilter (Intercept incoming call)
        CommonLibrary->>CommonLibrary: Parse X-User-Id, X-User-Roles
        CommonLibrary->>TargetService: Establish SecurityContextHolder
    end

    %% Validation & Outbox
    rect rgb(255, 250, 240)
        note right of TargetService: Standard Processing & Messaging
        TargetService->>CommonLibrary: Consume @Valid annotations & DTOs
        TargetService->>CommonLibrary: Publish Event via Outbox Pattern
    end
```
