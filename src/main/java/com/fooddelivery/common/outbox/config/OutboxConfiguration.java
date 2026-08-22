package com.fooddelivery.common.outbox.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Bean;
import com.fooddelivery.common.outbox.service.OutboxProcessor;
import com.fooddelivery.common.outbox.repository.OutboxEventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Wires the transactional outbox (entity, repository, processor) into any service
 * that scans {@code com.fooddelivery}.
 *
 * <p>The outbox is JPA-backed, so this configuration requires an
 * {@code EntityManagerFactory}. Services without a datasource — or tests that
 * exclude {@code DataSourceAutoConfiguration} — must opt out by setting
 * {@code outbox.enabled=false}, otherwise the context fails on a
 * missing {@code entityManagerFactory} bean.
 *
 * <p>Defaults to enabled so that every service already relying on the outbox
 * keeps working without configuration changes.
 */
import org.springframework.context.annotation.Bean;
import com.fooddelivery.common.outbox.service.OutboxProcessor;
import com.fooddelivery.common.outbox.repository.OutboxEventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import io.micrometer.core.instrument.MeterRegistry;

@ConditionalOnProperty(name = "outbox.enabled", havingValue = "true", matchIfMissing = true)
@EnableJpaRepositories(basePackages = {"com.fooddelivery.common.outbox.repository"})
@EntityScan(basePackages = "com.fooddelivery.common.outbox.entity")
public class OutboxConfiguration {

    @Bean
    public OutboxProcessor outboxProcessor(OutboxEventRepository repository, 
                                           KafkaTemplate<String, String> kafkaTemplate,
                                           MeterRegistry meterRegistry) {
        return new OutboxProcessor(repository, kafkaTemplate, meterRegistry);
    }
}
