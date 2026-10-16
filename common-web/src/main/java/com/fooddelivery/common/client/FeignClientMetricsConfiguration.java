package com.fooddelivery.common.client;

import feign.micrometer.DefaultFeignObservationConvention;
import feign.micrometer.MicrometerObservationCapability;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * Feign client metrics under their own name, {@code feign.client.requests}, for every service that adds
 * feign-micrometer. Under Spring Cloud OpenFeign's default the Feign observation is also named
 * {@code http.client.requests}, which Eureka's RestTemplate registers first with other tag keys; Prometheus needs one
 * tag-key set per name, so the scrape silently dropped every Feign series on Dev while an in-memory registry still
 * had them (ServiceDecomposition Phase 11, measured with a probe test). Spring Cloud's own capability bean is
 * {@code @ConditionalOnMissingBean}, so this one replaces it in every Feign client context.
 */
@AutoConfiguration(afterName = "org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration")
@ConditionalOnClass(name = "feign.micrometer.MicrometerObservationCapability")
public class FeignClientMetricsConfiguration {

    public static final String METRIC = "feign.client.requests";

    @Bean
    @ConditionalOnBean(ObservationRegistry.class)
    public MicrometerObservationCapability feignClientMetricsCapability(ObservationRegistry registry) {
        return new MicrometerObservationCapability(registry, new DefaultFeignObservationConvention() {
            @Override
            public String getName() {
                return METRIC;
            }
        });
    }
}
