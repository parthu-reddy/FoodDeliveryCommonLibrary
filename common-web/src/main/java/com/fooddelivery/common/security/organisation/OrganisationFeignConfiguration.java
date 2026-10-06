package com.fooddelivery.common.security.organisation;

import feign.Retryer;

import org.springframework.context.annotation.Bean;

/** Feign-only configuration: organisation and restaurant access lookups never retry. */
public class OrganisationFeignConfiguration {
    @Bean
    public Retryer organisationRetryer() {
        return Retryer.NEVER_RETRY;
    }
}
