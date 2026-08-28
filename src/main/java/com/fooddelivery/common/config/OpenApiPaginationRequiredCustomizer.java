package com.fooddelivery.common.config;

import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

/**
 * Marks the always-present properties of Spring Data's pagination schemas as {@code required}.
 *
 * <p>Forty-four of the ninety-three schemas that declared nothing {@code required} on 2026-08-27
 * were Spring's own types — {@code Page*}, {@code PageableObject}, {@code SortObject},
 * {@code Pageable}. There is no DTO of ours to annotate with {@code @NotNull}, so the usual fix does
 * not reach them, and {@code openapi-zod-client} emits {@code .partial()} for every one: a generated
 * validator that accepts {@code {}} as a valid page of results.
 *
 * <p>Doing it here rather than per module is the same reasoning as
 * {@link OpenApiJsonMediaTypeCustomizer}: there is nothing for anyone to remember, and a new
 * paginated endpoint is strict the day it is written.
 *
 * <p><strong>The key sets below are measured, not assumed.</strong>
 * {@code PageSerializationShapeTest} serialises a populated and an empty {@code PageImpl} and
 * asserts every name in {@link #PAGE_ALWAYS_PRESENT} appears in both. If a Spring upgrade changes
 * the shape, that test fails before these specs start making a promise the API does not keep.
 *
 * <p>Only properties the schema actually declares are marked, so a projection that omits one is
 * left alone rather than being given a {@code required} it cannot satisfy.
 */
@Configuration
public class OpenApiPaginationRequiredCustomizer {

    /** Keys a Spring Data {@code Page} emits whether or not it has content. */
    public static final List<String> PAGE_ALWAYS_PRESENT = List.of(
            "content", "totalElements", "totalPages", "size", "number",
            "numberOfElements", "first", "last", "empty");

    public static final List<String> PAGEABLE_ALWAYS_PRESENT = List.of(
            "pageNumber", "pageSize", "offset", "paged", "unpaged");

    /**
     * The three booleans a real {@code sort} carries on the wire. springdoc infers {@code SortObject}
     * from {@code Sort.Order} and models {@code sort} as an ARRAY of
     * {direction, property, ascending, ignoreCase, nullHandling} -- but Jackson serialises Spring's
     * {@code Sort} as an OBJECT of exactly these keys. Measured by PageSerializationShapeTest:
     * {@code SORT KEYS: [empty, sorted, unsorted]}.
     *
     * <p>The shape is corrected below before the required-marking runs, so these can then be marked
     * required like every other pagination field rather than left alone to avoid deepening a lie.
     */
    public static final List<String> SORT_ALWAYS_PRESENT = List.of("empty", "sorted", "unsorted");

    private static final List<String> PAGE_METADATA_ALWAYS_PRESENT = List.of(
            "size", "number", "totalElements", "totalPages");

    @Bean
    public OpenApiCustomizer paginationRequiredOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }
            correctSortShape(openApi.getComponents().getSchemas());

            openApi.getComponents().getSchemas().forEach((name, schema) -> {
                if (name == null || schema == null) {
                    return;
                }
                if ("SortObject".equals(name)) {
                    markRequired(schema, SORT_ALWAYS_PRESENT);
                    return;
                }
                if (name.startsWith("Page") && !isKnownHelper(name)) {
                    markRequired(schema, PAGE_ALWAYS_PRESENT);
                } else if ("PageableObject".equals(name) || "Pageable".equals(name)) {
                    markRequired(schema, PAGEABLE_ALWAYS_PRESENT);
                } else if ("PageMetadata".equals(name)) {
                    markRequired(schema, PAGE_METADATA_ALWAYS_PRESENT);
                }
            });
        };
    }


    /**
     * Rewrites {@code SortObject} to the shape the wire actually uses, and repoints every
     * {@code sort} property at it as an object rather than an array.
     *
     * <p>Without this, a generated client models {@code sort} as {@code Sort.Order[]} and every
     * response fails validation, which is why the frontend validators had to be left permissive here.
     */
    private void correctSortShape(java.util.Map<String, Schema> schemas) {
        Schema<?> sortObject = schemas.get("SortObject");
        if (sortObject == null) {
            return;
        }
        java.util.Map<String, Schema> props = new java.util.LinkedHashMap<>();
        for (String key : SORT_ALWAYS_PRESENT) {
            props.put(key, new io.swagger.v3.oas.models.media.BooleanSchema());
        }
        sortObject.setType("object");
        sortObject.setProperties(props);
        sortObject.setItems(null);

        // Page.sort and PageableObject.sort are both declared as arrays of SortObject by springdoc.
        for (Schema<?> schema : schemas.values()) {
            if (schema == null || schema.getProperties() == null) {
                continue;
            }
            Object sort = schema.getProperties().get("sort");
            if (sort instanceof Schema<?> sortProp && "array".equals(sortProp.getType())) {
                sortProp.setType(null);
                sortProp.setItems(null);
                sortProp.set$ref("#/components/schemas/SortObject");
            }
        }
    }

    /** {@code PageableObject}, {@code PageMetadata} and {@code Pageable} start with "Page" but are not pages. */
    private static boolean isKnownHelper(String name) {
        return Set.of("PageableObject", "Pageable", "PageMetadata").contains(name);
    }

    private void markRequired(Schema<?> schema, List<String> candidates) {
        if (schema.getProperties() == null) {
            return;
        }
        for (String property : candidates) {
            if (schema.getProperties().containsKey(property)
                    && (schema.getRequired() == null || !schema.getRequired().contains(property))) {
                schema.addRequiredItem(property);
            }
        }
    }
}
