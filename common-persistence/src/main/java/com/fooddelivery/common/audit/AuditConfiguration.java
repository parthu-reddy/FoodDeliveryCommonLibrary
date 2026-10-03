package com.fooddelivery.common.audit;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;

/** Database-free services must not instantiate components that require a persistence context. */
@AutoConfiguration(after = HibernateJpaAutoConfiguration.class)
@ConditionalOnBean(EntityManagerFactory.class)
public class AuditConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public AuditReader auditReader() {
        return new AuditReader();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditTrail auditTrail() {
        return new AuditTrail();
    }
}
