package com.fooddelivery.common.audit;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class AuditConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AuditConfiguration.class));

    @Test
    void databaseFreeContextDoesNotRequireAuditPersistence() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(EntityManagerFactory.class);
            assertThat(context).doesNotHaveBean(AuditReader.class);
            assertThat(context).doesNotHaveBean(AuditTrail.class);
        });
    }

    @Test
    void databaseContextProvidesBothAuditComponents() {
        databaseRunner().run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(EntityManagerFactory.class);
            assertThat(context).hasSingleBean(AuditReader.class);
            assertThat(context).hasSingleBean(AuditTrail.class);
            // Exercise the injected EntityManager rather than only checking bean names.
            assertThat(context.getBean(AuditReader.class)
                    .history("ORGANISATION", java.util.UUID.randomUUID(), 0, 10)).isEmpty();
        });
    }

    private ApplicationContextRunner databaseRunner() {
        return runner.withConfiguration(AutoConfigurations.of(DataSourceAutoConfiguration.class,
                        HibernateJpaAutoConfiguration.class))
                .withUserConfiguration(Entities.class)
                .withPropertyValues("spring.datasource.url=jdbc:h2:mem:audit_configuration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                        "spring.jpa.hibernate.ddl-auto=create-drop");
    }

    @Configuration(proxyBeanMethods = false)
    @EntityScan(basePackageClasses = AuditEvent.class)
    static class Entities { }
}
