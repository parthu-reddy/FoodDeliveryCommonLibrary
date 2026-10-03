package com.fooddelivery.common.security.organisation;

import com.fooddelivery.common.client.OrganisationServiceClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.*;

/** Explicit registration for services whose own Feign scan does not include common clients. */
@AutoConfiguration
@ConditionalOnMissingBean(OrganisationServiceClient.class)
@EnableFeignClients(clients=OrganisationServiceClient.class)
public class OrganisationClientConfiguration { }
