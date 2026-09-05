package com.fooddelivery.common.idempotency;

import com.fooddelivery.common.repository.IIdempotencyKeyRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * Registers {@link IdempotencyKeySweeper} wherever idempotency keys can be written.
 *
 * <p>Conditions mirror {@code OutboxConfiguration}: a datasource must exist and JPA must be on the
 * classpath. Switch it off with {@code idempotency.sweep.enabled=false} for a service that has a
 * database but deliberately retains keys.
 */
@AutoConfiguration(after = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ConditionalOnClass(DataSource.class)
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(name = "idempotency.sweep.enabled", havingValue = "true", matchIfMissing = true)
public class IdempotencySweepConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdempotencyKeySweeper idempotencyKeySweeper(
            ObjectProvider<IIdempotencyKeyRepository> repositoryProvider,
            @org.springframework.beans.factory.annotation.Value("${idempotency.sweep.retention-days:7}") int retentionDays) {
        return new IdempotencyKeySweeper(repositoryProvider, retentionDays);
    }
}
