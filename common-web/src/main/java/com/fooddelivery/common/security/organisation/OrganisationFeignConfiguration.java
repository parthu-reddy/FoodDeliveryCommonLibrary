package com.fooddelivery.common.security.organisation;

import feign.Retryer;
import org.springframework.context.annotation.Bean;

/** Feign-only configuration, outside component scanning: membership lookups never retry. */
public class OrganisationFeignConfiguration {
    @Bean public Retryer organisationRetryer() { return Retryer.NEVER_RETRY; }
}
