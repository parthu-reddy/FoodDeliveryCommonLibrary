package com.fooddelivery.common.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

/**
 * One 404 rule for every Feign circuit breaker: a 404 is the callee's answer, not a failure of the callee, so a burst of
 * unknown ids must not open the breaker ({@link FeignFailures.NotFoundIsAnAnswer}).
 *
 * <p>Set here, as the lowest-precedence default of every service that has common-web, rather than in
 * {@code platform-defaults.yml}: ApiGateway imports that file and binds resilience4j but does not have common-web, so the
 * predicate class would not load there. Until 2026-10-17 only the four split services set it in their own
 * {@code application.yml}; every other breaker counted a 404 as a failure.
 */
public class FeignBreakerDefaults implements EnvironmentPostProcessor, Ordered {

    static final String SOURCE = "commonWebFeignBreakerDefaults";
    static final String IGNORE_PREDICATE = "resilience4j.circuitbreaker.configs.default.ignore-exception-predicate";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.getPropertySources().contains(SOURCE)) {
            environment.getPropertySources().addLast(new MapPropertySource(SOURCE,
                    Map.of(IGNORE_PREDICATE, FeignFailures.NotFoundIsAnAnswer.class.getName())));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
