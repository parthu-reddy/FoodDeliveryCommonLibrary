package com.fooddelivery.common.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.base.DescribedPredicate;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public class ArchUnitRules {

    public static final DescribedPredicate<JavaClass> isGeneratedOrImpl = 
        DescribedPredicate.describe("is generated or impl", 
            clazz -> clazz.getSimpleName().endsWith("Impl") 
                  || clazz.isAnnotatedWith("jakarta.annotation.Generated") 
                  || clazz.isAnnotatedWith("javax.annotation.processing.Generated"));

    public static final DescribedPredicate<JavaClass> anyClass = 
        DescribedPredicate.alwaysTrue();

    /**
     * Enforces that controllers must return ResponseEntity or structured wrappers,
     * not raw collections or objects, to ensure valid OpenAPI schemas.
     */
    public static final ArchRule controllers_must_return_standard_wrappers = 
        com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods()
            .that().areDeclaredInClassesThat().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .and().arePublic()
            .should().haveRawReturnType(org.springframework.http.ResponseEntity.class)
            .because("All endpoints must wrap their outputs in structured DTOs (e.g., PageResponseDto) to ensure OpenAPI schema generation is strictly typed for the UI.");

    /**
     * Provides a base layered architecture rule that can be reused across all Food Delivery microservices.
     * Specific services can chain additional constraints or exemptions (like ignoreDependency) if needed.
     */
    public static ArchRule getBaseLayeredArchitecture() {
        return layeredArchitecture()
            .consideringAllDependencies()
            .withOptionalLayers(true)
            .layer("Controller").definedBy("..controller..", "..kafka..", "..messaging..", "..beckn.bpp..", "..listener..", "..websocket..")
            .layer("Service").definedBy("..service..", "..refund..", "..scheduler..", "..security..", "..job..", "..matcher..", "..catalog..", "..settlement..", "..reconciliation..", "..event..")
            .layer("Repository").definedBy("..repository..")
            .layer("Client").definedBy("..client..")
            .layer("Config").definedBy("..config..")
            .layer("Mapper").definedBy("..mapper..")
            .layer("Filter").definedBy("..filter..")
            .layer("DTO").definedBy("..dto..", "..entity..")
            
            // Controllers shouldn't be called by anyone except Configs (e.g. for security setup) or tests.
            // MCP service is an AI tool integration, it's essentially acting as a mega-controller.
            .whereLayer("Controller").mayOnlyBeAccessedByLayers("Config", "Service") 
            
            // Services hold business logic. They are called by Controllers, other Services, Configs, DTOs (for types), and Clients (which return Service inner DTOs), and Filters (e.g. Auth filters accessing token services)
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Service", "Config", "DTO", "Client", "Filter")
            
            // Repositories can be called by Services, Configs, Controllers, Filters in this legacy codebase.
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service", "Controller", "Config", "Filter")
            
            // Feign Clients are called by Services and Controllers
            .whereLayer("Client").mayOnlyBeAccessedByLayers("Service", "Config", "Controller")
            
            // Ignore MapStruct generated code
            .ignoreDependency(isGeneratedOrImpl, anyClass);
    }
}
