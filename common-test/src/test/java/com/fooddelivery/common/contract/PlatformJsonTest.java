package com.fooddelivery.common.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.time.Instant;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformJsonTest {

    record Summary(String name, Instant lastSeenAt) {}

    @Test
    void harnessesWriteInstantsTheWayServicesDo() throws Exception {
        String json = PlatformJson.objectMapper().writeValueAsString(new Summary("x", Instant.parse("2023-10-01T12:00:00Z")));
        assertThat(json).isEqualTo("{\"name\":\"x\",\"lastSeenAt\":\"2023-10-01T12:00:00Z\"}");
    }

    @Test
    void aBareMapperIsWhyThisExists() throws Exception {
        // What standalone MockMvc used to hand the GovernmentID summary contract.
        String bare = new ObjectMapper().findAndRegisterModules().writeValueAsString(Instant.parse("2023-10-01T12:00:00Z"));
        assertThat(bare).isEqualTo("1696161600.000000000");
    }

    @Test
    void theConvertersCarryThatMapper() {
        assertThat(Arrays.stream(PlatformJson.messageConverters())
                .filter(MappingJackson2HttpMessageConverter.class::isInstance)
                .map(c -> ((MappingJackson2HttpMessageConverter) c).getObjectMapper()))
                .containsExactly(PlatformJson.objectMapper());
    }
}
