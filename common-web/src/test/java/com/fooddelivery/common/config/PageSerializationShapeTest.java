package com.fooddelivery.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Establishes, by serialising rather than by assumption, which keys a Spring Data {@code Page}
 * always emits. {@link OpenApiPaginationRequiredCustomizer} marks exactly these as {@code required},
 * so if a Spring upgrade changes the shape this test fails before the specs start lying.
 */
class PageSerializationShapeTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void pageAlwaysEmitsTheKeysTheCustomizerMarksRequired() throws Exception {
        var paged = mapper.readTree(mapper.writeValueAsString(
                new PageImpl<>(List.of("a"), PageRequest.of(0, 10, Sort.by("id")), 1)));
        var empty = mapper.readTree(mapper.writeValueAsString(
                new PageImpl<>(List.<String>of(), PageRequest.of(0, 10), 0)));

        var pagedKeys = new TreeSet<String>();
        paged.fieldNames().forEachRemaining(pagedKeys::add);
        var emptyKeys = new TreeSet<String>();
        empty.fieldNames().forEachRemaining(emptyKeys::add);

        System.out.println("PAGE KEYS (populated): " + pagedKeys);
        System.out.println("PAGE KEYS (empty)    : " + emptyKeys);
        System.out.println("PAGEABLE KEYS        : " + fields(paged, "pageable"));
        System.out.println("SORT KEYS            : " + fields(paged, "sort"));

        // The unpaged case: serialising it does not omit keys, it THROWS. Unpaged.getPageSize()
        // raises UnsupportedOperationException, so such a response never reaches a client at all.
        // That means "required" is safe for anything that serialises successfully -- and that an
        // endpoint returning an unpaged Page is a 500, which is a separate finding.
        assertThrows(com.fasterxml.jackson.databind.JsonMappingException.class,
                () -> mapper.writeValueAsString(
                        new PageImpl<>(List.of("a"), org.springframework.data.domain.Pageable.unpaged(), 1)),
                "an unpaged Page now serialises; re-derive whether pageable's keys are still always present");

        for (String required : OpenApiPaginationRequiredCustomizer.PAGEABLE_ALWAYS_PRESENT) {
            assertTrue(fields(paged, "pageable").contains(required),
                    "paged pageable is missing '" + required + "'");
        }
    }

    private static TreeSet<String> fields(com.fasterxml.jackson.databind.JsonNode root, String name) {
        var out = new TreeSet<String>();
        var node = root.get(name);
        if (node != null) node.fieldNames().forEachRemaining(out::add);
        return out;
    }
}
