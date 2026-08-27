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

    /*
     * SortObject is deliberately NOT marked required. The spec models Page.sort as an array of
     * SortObject -- {direction, property, ascending, ignoreCase, nullHandling}, i.e. Sort.Order --
     * but a real Page serialises `sort` as an OBJECT, {empty, sorted, unsorted}. Measured
     * 2026-08-27 by PageSerializationShapeTest. The spec is already wrong about this field, so
     * asserting that its properties are always present would make the spec lie harder rather than
     * less. Fixing the shape is a separate change; see the phase notes.
     */

    private static final List<String> PAGE_METADATA_ALWAYS_PRESENT = List.of(
            "size", "number", "totalElements", "totalPages");

    @Bean
    public OpenApiCustomizer paginationRequiredOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }
            openApi.getComponents().getSchemas().forEach((name, schema) -> {
                if (name == null || schema == null) {
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
