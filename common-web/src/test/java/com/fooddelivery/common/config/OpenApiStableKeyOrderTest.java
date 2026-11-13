package com.fooddelivery.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The OpenAPI writer must not depend on the order a JVM run happens to build the model in.
 *
 * <p>The two documents below hold the same API, built in opposite orders: the paths, the schemas and each
 * schema's properties. That is what two JVM runs hand the writer when {@code getDeclaredMethods()} comes back in
 * a different order. Both must serialise to the same bytes, and every object in the output must have its keys
 * in alphabetical order, including the fields of a {@code Schema} bean ({@code default}, {@code enum},
 * {@code type}) that a map sort alone leaves in the JVM's order.
 */
class OpenApiStableKeyOrderTest {

    private static final List<String> PAGE_FIELDS = List.of(
            "sort", "offset", "pageable", "first", "last", "numberOfElements", "content", "number", "size", "empty");

    @Test
    void sameApiBuiltInOppositeOrdersWritesIdenticalBytes() throws Exception {
        // swagger-core uses this shared mapper while it resolves a model, before springdoc writes anything; the
        // Schema serializer it caches here is the one springdoc.writer-with-order-by-keys could no longer sort.
        Json.mapper().writeValueAsString(statusParameterSchema());

        ObjectMapper writer = provider().jsonMapper();
        String forward = writer.writerWithDefaultPrettyPrinter().writeValueAsString(api(false));
        String reverse = writer.writerWithDefaultPrettyPrinter().writeValueAsString(api(true));

        assertThat(reverse).isEqualTo(forward);
        assertThat(unsortedObjects(forward)).isEmpty();
        assertThat(forward).contains("\"default\" : \"IN_REVIEW\",\n");
    }

    @Test
    void replacesSpringdocsOwnProvider() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        JacksonAutoConfiguration.class, WebMvcAutoConfiguration.class, OpenApiStableKeyOrder.class, SpringDocConfiguration.class,
                        SpringDocConfigProperties.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(ObjectMapperProvider.class);
                    assertThat(context.getBean(ObjectMapperProvider.class))
                            .isInstanceOf(OpenApiStableKeyOrder.SortedObjectMapperProvider.class);
                });
    }

    private static ObjectMapperProvider provider() {
        return new OpenApiStableKeyOrder.SortedObjectMapperProvider(new SpringDocConfigProperties());
    }

    private static OpenAPI api(boolean reversed) {
        Map<String, Schema> pageProperties = new LinkedHashMap<>();
        for (String field : ordered(PAGE_FIELDS, reversed)) {
            pageProperties.put(field, field.equals("first") || field.equals("last") || field.equals("empty")
                    ? new BooleanSchema() : new IntegerSchema());
        }
        Schema<?> page = new ObjectSchema();
        page.setProperties(pageProperties);

        Map<String, Schema> schemas = new LinkedHashMap<>();
        for (String name : ordered(List.of("SliceAuditEvent", "PageableObject", "SortObject"), reversed)) {
            schemas.put(name, page);
        }

        Paths paths = new Paths();
        for (String path : ordered(List.of("/api/v1/z", "/api/v1/a", "/api/v1/m"), reversed)) {
            paths.addPathItem(path, new PathItem().get(new Operation().operationId("op" + path.length())
                    .addParametersItem(new Parameter().name("status").in("query").schema(statusParameterSchema()))));
        }
        return new OpenAPI().paths(paths).components(new Components().schemas(schemas));
    }

    private static Schema<?> statusParameterSchema() {
        StringSchema status = new StringSchema();
        status.setEnum(List.of("DRAFT", "SUBMITTED", "IN_REVIEW"));
        status.setDefault("IN_REVIEW");
        return status;
    }

    private static List<String> ordered(List<String> names, boolean reversed) {
        List<String> copy = new ArrayList<>(names);
        if (reversed) {
            java.util.Collections.reverse(copy);
        }
        return copy;
    }

    /** Every object whose keys are not in ascending order, described by its keys. */
    private static List<String> unsortedObjects(String json) throws Exception {
        List<String> unsorted = new ArrayList<>();
        Deque<List<String>> open = new ArrayDeque<>();
        try (JsonParser parser = new ObjectMapper().getFactory().createParser(json)) {
            for (JsonToken token; (token = parser.nextToken()) != null; ) {
                if (token == JsonToken.START_OBJECT) {
                    open.push(new ArrayList<>());
                } else if (token == JsonToken.FIELD_NAME) {
                    open.peek().add(parser.currentName());
                } else if (token == JsonToken.END_OBJECT) {
                    List<String> keys = open.pop();
                    if (!keys.equals(keys.stream().sorted().toList())) {
                        unsorted.add(keys.toString());
                    }
                }
            }
        }
        return unsorted;
    }
}
