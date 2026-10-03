package com.fooddelivery.common.security.organisation;

import com.fooddelivery.common.client.OrganisationServiceClient;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after=OrganisationClientConfiguration.class)
public class OrganisationAccessConfiguration {
    @Bean @ConditionalOnMissingBean(com.fooddelivery.common.client.OrganisationServiceClientFallback.class)
    public com.fooddelivery.common.client.OrganisationServiceClientFallback organisationServiceClientFallback() {
        return new com.fooddelivery.common.client.OrganisationServiceClientFallback();
    }
    @Bean(name="organisationAccessPolicy") @ConditionalOnMissingBean(OrganisationAccessPolicy.class)
    public OrganisationAccessPolicy organisationAccessPolicy(@org.springframework.context.annotation.Lazy OrganisationServiceClient client,org.springframework.beans.factory.ObjectProvider<MeterRegistry> metrics) {
        return new DefaultOrganisationAccessPolicy(client,metrics.getIfAvailable(io.micrometer.core.instrument.simple.SimpleMeterRegistry::new));
    }
}
