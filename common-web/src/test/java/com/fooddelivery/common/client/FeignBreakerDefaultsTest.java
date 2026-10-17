package com.fooddelivery.common.client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FeignBreakerDefaultsTest {

    @Test
    void everyServiceWithCommonWebIgnoresA404InItsBreakers() {
        StandardEnvironment env = new StandardEnvironment();
        new FeignBreakerDefaults().postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty(FeignBreakerDefaults.IGNORE_PREDICATE))
                .isEqualTo("com.fooddelivery.common.client.FeignFailures$NotFoundIsAnAnswer");
    }

    @Test
    void anyConfiguredValueOutranksTheDefault() {
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addFirst(new MapPropertySource("service",
                Map.of(FeignBreakerDefaults.IGNORE_PREDICATE, "x.Other")));
        new FeignBreakerDefaults().postProcessEnvironment(env, new SpringApplication());

        assertThat(env.getProperty(FeignBreakerDefaults.IGNORE_PREDICATE)).isEqualTo("x.Other");
        assertThat(env.getPropertySources().stream().reduce((a, b) -> b).orElseThrow().getName())
                .isEqualTo(FeignBreakerDefaults.SOURCE);
    }

    @Test
    void springBootRunsItFromSpringFactories() {
        // A plain Boot run applies it: registered in META-INF/spring.factories, nothing else sets the key.
        try (ConfigurableApplicationContext ctx = new SpringApplicationBuilder(Empty.class)
                .web(WebApplicationType.NONE).properties("spring.main.banner-mode=off", "spring.cloud.config.enabled=false").run()) {
            assertThat(ctx.getEnvironment().getProperty(FeignBreakerDefaults.IGNORE_PREDICATE))
                    .isEqualTo(FeignFailures.NotFoundIsAnAnswer.class.getName());
        }
    }

    @org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
    static class Empty {
    }
}
