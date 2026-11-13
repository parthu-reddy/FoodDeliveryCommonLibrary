package com.fooddelivery.common.config;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Makes every service write its OpenAPI document byte-for-byte the same on every run: the properties of each object
 * in alphabetical order, the entries of each map (paths, schemas, a schema's properties, responses) by key.
 *
 * <p><strong>Why.</strong> swagger-core and Jackson take property order from {@code Class.getDeclaredMethods()}, and
 * HotSpot does not keep that order between JVM runs (measured 2026-11-13: four launches printed Spring's {@code Slice}
 * methods in four orders). So {@code Page}/{@code Slice} schema properties and a schema's own {@code default} and
 * {@code enum} moved between builds. The committed {@code openapi.json} files and the 27 generated UI client files
 * were rewritten by releases whose API had not changed, and every phase record built on them went stale.
 *
 * <p><strong>Why not {@code springdoc.writer-with-order-by-keys}.</strong> That flag reconfigures swagger's shared
 * static {@code Json.mapper()} at write time. By then swagger-core has already used that mapper and cached its
 * {@code Schema} serializer, so maps come out sorted but a schema's own fields keep the JVM's order (seen in a probe
 * on springdoc 2.5.0: {@code type, enum, default}). A copy made here, configured before its first use, sorts both.
 *
 * <p>springdoc registers its own provider only {@code @ConditionalOnMissingBean}, so this one replaces it. The bean
 * has its own name: with {@code spring.main.allow-bean-definition-overriding=true} (every spec test sets it) a
 * same-named definition could silently replace this one instead of failing.
 * The conditions mirror {@link SpringDocConfiguration}'s, whose properties bean this needs.
 */
@AutoConfiguration(before = SpringDocConfiguration.class)
@ConditionalOnClass(ObjectMapperProvider.class)
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", matchIfMissing = true)
public class OpenApiStableKeyOrder {

    @Bean
    public ObjectMapperProvider stableKeyOrderObjectMapperProvider(SpringDocConfigProperties properties) {
        return new SortedObjectMapperProvider(properties);
    }

    /** springdoc's provider with both of its mappers replaced by sorted copies. */
    public static final class SortedObjectMapperProvider extends ObjectMapperProvider {

        private final ObjectMapper jsonMapper;
        private final ObjectMapper yamlMapper;

        public SortedObjectMapperProvider(SpringDocConfigProperties properties) {
            super(properties);
            this.jsonMapper = sorted(super.jsonMapper());
            this.yamlMapper = sorted(super.yamlMapper());
        }

        @Override
        public ObjectMapper jsonMapper() {
            return jsonMapper;
        }

        @Override
        public ObjectMapper yamlMapper() {
            return yamlMapper;
        }

        /** {@code copy()} starts with an empty serializer cache, so the features below reach every type. */
        private static ObjectMapper sorted(ObjectMapper shared) {
            return shared.copy()
                    .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        }
    }
}
