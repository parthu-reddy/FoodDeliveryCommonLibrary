package com.fooddelivery.common.outbox.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.outbox.repository.OutboxEventRepository;
import com.fooddelivery.common.outbox.service.OutboxProcessor;
import com.fooddelivery.common.service.NotificationRouterService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.KafkaTemplate;

import javax.sql.DataSource;

/**
 * Wires the transactional outbox — entity, repository, processor, and the notification router that
 * writes through it — into any service that has a datasource.
 *
 * <p>This is a Spring Boot auto-configuration, registered in
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}. It is
 * not component-scanned, and services do not opt in by annotation.
 *
 * <p>It used to be opt-in, via an {@code @EnableOutbox} annotation that {@code @Import}ed this
 * class — which was the only thing making it a configuration class at all, since it never carried
 * {@code @Configuration}. Every service that wrote outbox rows did remember the annotation, so
 * nothing was silently unpublished. The cost was paid at the other end: three services that scan
 * {@code com.fooddelivery.common} but have no datasource — BiddingEngine, UserTrackingService and
 * CommunicationIntegration — could not start at all, because {@link NotificationRouterService} was
 * an unconditional {@code @Service} requiring an {@link OutboxEventRepository} they had no way to
 * provide.
 *
 * <p>Auto-configuring on the presence of a datasource removes both halves of that: a service with a
 * database gets a working outbox without asking, and a service without one is not asked to supply
 * infrastructure it does not use. Note that no test would have reported a missing processor
 * either way — every contract test constructs an {@link OutboxProcessor} by hand, so none of them
 * exercises the wiring.
 *
 * <p>Conditions mirror Spring Boot's own {@code JpaRepositoriesAutoConfiguration}: a datasource must
 * exist, JPA must be on the classpath, and the whole thing can be switched off with
 * {@code outbox.enabled=false} for a service that has a database but wants no outbox.
 */
@AutoConfiguration(after = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ConditionalOnClass({EntityManagerFactory.class, DataSource.class})
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(name = "outbox.enabled", havingValue = "true", matchIfMissing = true)
@EnableJpaRepositories(basePackages = "com.fooddelivery.common.outbox.repository")
@EntityScan(basePackages = "com.fooddelivery.common.outbox.entity")
public class OutboxConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OutboxProcessor outboxProcessor(OutboxEventRepository repository,
                                           KafkaTemplate<String, String> kafkaTemplate,
                                           MeterRegistry meterRegistry) {
        return new OutboxProcessor(repository, kafkaTemplate, meterRegistry);
    }

    /**
     * Writes notification requests through the outbox, so it exists only where the outbox does.
     * It was previously an unconditional {@code @Service}, which forced every service scanning
     * {@code com.fooddelivery.common} to supply an {@link OutboxEventRepository} or fail to boot.
     */
    @Bean
    @ConditionalOnMissingBean
    public NotificationRouterService notificationRouterService(OutboxEventRepository repository,
                                                              ObjectMapper objectMapper) {
        return new NotificationRouterService(repository, objectMapper);
    }
}
