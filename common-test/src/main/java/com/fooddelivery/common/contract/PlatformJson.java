package com.fooddelivery.common.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.config.JacksonConfig;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * The JSON the services actually send, for contract harnesses.
 *
 * <p>Standalone MockMvc doesn't receive Spring Boot's Jackson auto-configuration. Left at its
 * default, a harness writes an {@code Instant} as {@code 1696161600.000000000}, a shape no service
 * sends, and the contract recorded through it plus the stub built from that contract hand every
 * consumer something production never produces. Measured 2026-09-25 when GovernmentIDValidationService's
 * summary contract met its first typed timestamp. By then 13 of the 14 harnesses in the workspace were
 * built this way. See memory: contract-harness-jackson-drift.
 *
 * <p>The mapper here is not assembled by hand to resemble the application's. It is taken from the
 * same two configuration classes every service runs ({@link JacksonAutoConfiguration} and the
 * platform {@link JacksonConfig}), so it can't drift.
 */
public final class PlatformJson {

    private PlatformJson() {}

    private static final class Holder {
        private static final ObjectMapper MAPPER = build();

        private static ObjectMapper build() {
            try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
                ctx.register(JacksonAutoConfiguration.class, JacksonConfig.class);
                ctx.refresh();
                return ctx.getBean(ObjectMapper.class);
            }
        }
    }

    /** The platform ObjectMapper, as every service's Boot context builds it. */
    public static ObjectMapper objectMapper() {
        return Holder.MAPPER;
    }

    /** Converters for {@code MockMvcBuilders.standaloneSetup(...).setMessageConverters(...)}. */
    public static HttpMessageConverter<?>[] messageConverters() {
        return new HttpMessageConverter<?>[] {
            new StringHttpMessageConverter(),
            new MappingJackson2HttpMessageConverter(objectMapper())
        };
    }

    /**
     * {@code RestAssuredMockMvc.standaloneSetup(controllers...)}, serializing the way production does.
     * Use it in every contract base class instead of calling RestAssuredMockMvc directly.
     */
    public static void standaloneSetup(Object... controllers) {
        RestAssuredMockMvc.standaloneSetup(
                MockMvcBuilders.standaloneSetup(controllers).setMessageConverters(messageConverters()));
    }
}
