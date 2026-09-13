package com.fooddelivery.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Labels structured JSON responses as {@code application/json} instead of {@code * / *}.
 *
 * <p>springdoc emits {@code * / *} for any handler that does not declare {@code produces}, which is
 * almost all of them. That is harmless for humans reading the spec and fatal for code generation:
 * {@code openapi-zod-client} looks up {@code content["application/json"]} and falls back to
 * {@code z.any()} for anything else. The measured effect on 2026-08-23 was
 * <strong>211 of 212 generated frontend endpoints validating nothing on the response</strong>,
 * while the ESLint rule banning raw {@code fetch} justified itself on "runtime schema validation"
 * that was not happening.
 *
 * <p>Fixing it here rather than adding {@code produces} to ~150 controller methods keeps it from
 * regressing the next time someone adds an endpoint: there is nothing for them to remember.
 *
 * <p><strong>Only structured payloads are relabelled.</strong> A handler returning
 * {@code ResponseEntity<String>} is served by {@code StringHttpMessageConverter} as
 * {@code text/plain}, so calling it JSON would be a lie that breaks the client's parse. Those keep
 * the wildcard, and their generated schema stays {@code z.any()} — which is accurate, because an
 * unstructured string has no shape to validate.
 */
@Configuration
public class OpenApiJsonMediaTypeCustomizer {

    private static final String WILDCARD = "*/*";
    private static final String JSON = "application/json";

    @Bean
    public OpenApiCustomizer jsonMediaTypeOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(this::relabelResponses));
        };
    }

    private void relabelResponses(Operation operation) {
        if (operation.getResponses() == null) {
            return;
        }
        for (ApiResponse response : operation.getResponses().values()) {
            Content content = response.getContent();
            if (content == null) {
                continue;
            }
            MediaType wildcard = content.get(WILDCARD);
            if (wildcard == null || content.containsKey(JSON)) {
                continue;
            }
            if (!isStructured(wildcard.getSchema())) {
                continue;
            }
            content.remove(WILDCARD);
            content.addMediaType(JSON, wildcard);
        }
    }

    /** A $ref, object or array serialises as JSON; a bare string or binary does not. */
    private boolean isStructured(Schema<?> schema) {
        if (schema == null) {
            return false;
        }
        if (schema.get$ref() != null) {
            return true;
        }
        String type = schema.getType();
        return "object".equals(type) || "array".equals(type);
    }
}
