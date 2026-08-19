package com.fooddelivery.common.outbox.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.fooddelivery.common.outbox.service")
@EnableJpaRepositories(basePackages = {"com.fooddelivery.common.outbox.repository"})
@EntityScan(basePackages = "com.fooddelivery.common.outbox.entity")
public class OutboxConfiguration {
}
