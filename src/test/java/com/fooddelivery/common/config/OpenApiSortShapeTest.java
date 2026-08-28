package com.fooddelivery.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * springdoc infers {@code SortObject} from {@code Sort.Order} and models a page's {@code sort} as an
 * ARRAY of {direction, property, ascending, ignoreCase, nullHandling}. Jackson serialises Spring's
 * {@code Sort} as an OBJECT of {empty, sorted, unsorted} -- confirmed at runtime by
 * PageSerializationShapeTest, which prints {@code SORT KEYS: [empty, sorted, unsorted]}.
 *
 * <p>A generated client built from the uncorrected spec expects an array and rejects every paged
 * response, which is why the frontend validators had to stay permissive around pagination.
 */
class OpenApiSortShapeTest {

    /** The spec springdoc produces before the customizer runs. */
    private OpenAPI springdocOutput() {
        Schema<?> sortObject = new ObjectSchema();
        sortObject.setProperties(new java.util.LinkedHashMap<>(Map.of(
                "direction", new StringSchema(),
                "property", new StringSchema(),
                "ascending", new io.swagger.v3.oas.models.media.BooleanSchema(),
                "ignoreCase", new io.swagger.v3.oas.models.media.BooleanSchema(),
                "nullHandling", new StringSchema())));

        ArraySchema sortAsArray = new ArraySchema();
        sortAsArray.setItems(new Schema<>().$ref("#/components/schemas/SortObject"));

        Schema<?> pageable = new ObjectSchema();
        pageable.setProperties(new java.util.LinkedHashMap<>(Map.of("sort", sortAsArray)));

        ArraySchema pageSortAsArray = new ArraySchema();
        pageSortAsArray.setItems(new Schema<>().$ref("#/components/schemas/SortObject"));
        Schema<?> page = new ObjectSchema();
        page.setProperties(new java.util.LinkedHashMap<>(Map.of("sort", pageSortAsArray)));

        OpenAPI api = new OpenAPI();
        api.setComponents(new Components().schemas(new java.util.LinkedHashMap<>(Map.of(
                "SortObject", (Schema) sortObject,
                "PageableObject", (Schema) pageable,
                "PageOrderResponse", (Schema) page))));
        return api;
    }

    private OpenAPI customized() {
        OpenAPI api = springdocOutput();
        new OpenApiPaginationRequiredCustomizer().paginationRequiredOpenApiCustomizer().customise(api);
        return api;
    }

    @Test
    void sortObjectMatchesTheWireShape() {
        Schema<?> sort = customized().getComponents().getSchemas().get("SortObject");
        assertEquals(List.of("empty", "sorted", "unsorted"),
                List.copyOf(sort.getProperties().keySet()),
                "SortObject must match what Jackson emits for Spring's Sort");
        assertEquals("object", sort.getType());
    }

    @Test
    void sortIsAnObjectReferenceNotAnArray() {
        var schemas = customized().getComponents().getSchemas();
        for (String owner : List.of("PageableObject", "PageOrderResponse")) {
            Schema<?> sortProp = (Schema<?>) schemas.get(owner).getProperties().get("sort");
            assertNull(sortProp.getType(), owner + ".sort must not still be an array");
            assertNull(sortProp.getItems(), owner + ".sort must not still have array items");
            assertEquals("#/components/schemas/SortObject", sortProp.get$ref(),
                    owner + ".sort must reference SortObject directly");
        }
    }

    @Test
    void theWireKeysAreMarkedRequired() {
        Schema<?> sort = customized().getComponents().getSchemas().get("SortObject");
        assertNotNull(sort.getRequired(), "SortObject should now carry required, the shape being correct");
        assertTrue(sort.getRequired().containsAll(List.of("empty", "sorted", "unsorted")));
    }

    @Test
    void staleSortOrderFieldsAreGone() {
        Schema<?> sort = customized().getComponents().getSchemas().get("SortObject");
        for (String stale : List.of("direction", "property", "ascending", "ignoreCase", "nullHandling")) {
            assertFalse(sort.getProperties().containsKey(stale),
                    stale + " is a Sort.Order field and never appears on the wire");
        }
    }
}
