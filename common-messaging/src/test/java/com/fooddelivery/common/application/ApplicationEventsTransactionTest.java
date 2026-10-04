package com.fooddelivery.common.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fooddelivery.common.audit.*;
import com.fooddelivery.common.constants.*;
import com.fooddelivery.common.enums.*;
import com.fooddelivery.common.outbox.config.OutboxConfiguration;
import com.fooddelivery.common.outbox.entity.OutboxEventEntity;
import com.fooddelivery.common.outbox.repository.OutboxEventRepository;
import com.fooddelivery.common.service.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Real database transactions: no outbox, audit, notification or counter survives a failed change. */
class ApplicationEventsTransactionTest {
    private ApplicationContextRunner runner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(DataSourceAutoConfiguration.class,
                        HibernateJpaAutoConfiguration.class, TransactionAutoConfiguration.class,
                        AuditConfiguration.class, OutboxConfiguration.class))
                .withUserConfiguration(Wiring.class)
                .withPropertyValues("spring.datasource.url=jdbc:h2:mem:application_events_" + UUID.randomUUID()
                                + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "spring.jpa.hibernate.ddl-auto=create-drop");
    }

    @Configuration(proxyBeanMethods = false) @EnableTransactionManagement
    @EntityScan(basePackageClasses = {AuditEvent.class, OutboxEventEntity.class, ApplicationState.class})
    static class Wiring {
        @Bean ObjectMapper mapper() { return new ObjectMapper().registerModule(new JavaTimeModule()); }
        @Bean SimpleMeterRegistry metrics() { return new SimpleMeterRegistry(); }
        @Bean KafkaTemplate<String, String> kafka() { return mock(KafkaTemplate.class); }
        @Bean EntityManager entityManager(EntityManagerFactory factory) {
            return SharedEntityManagerCreator.createSharedEntityManager(factory);
        }
    }

    @Entity(name = "ApplicationState") @Table(name = "test_application_state")
    public static class ApplicationState {
        @Id UUID id;
        @Enumerated(EnumType.STRING) ApplicationStatus status;
        protected ApplicationState() { }
        ApplicationState(UUID id) { this.id = id; status = ApplicationStatus.IN_REVIEW; }
    }

    @Test void commitsStateAuditTypedEventAndDeduplicatedNotificationsTogether() {
        runner().run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(ApplicationEvents.class);
            var em = context.getBean(EntityManager.class);
            var tx = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
            var metrics = context.getBean(SimpleMeterRegistry.class);
            var events = context.getBean(ApplicationEvents.class);
            UUID id = UUID.randomUUID(), org = UUID.randomUUID(), actor = UUID.randomUUID();
            UUID recipient = UUID.randomUUID(), other = UUID.randomUUID();
            seed(em, tx, id);
            tx.executeWithoutResult(status -> {
                em.find(ApplicationState.class, id).status = ApplicationStatus.APPROVED;
                events.restaurant(id, org, ApplicationStatus.APPROVED, null, 3, Instant.now(), actor,
                        "ADMIN", AuditAction.APPLICATION_APPROVED, List.of(recipient, recipient, other));
                assertThat(metrics.find("applications.transitions").counter()).isNull();
            });
            assertThat(em.find(ApplicationState.class, id).status).isEqualTo(ApplicationStatus.APPROVED);
            var audits = em.createQuery("select e from AuditEvent e", AuditEvent.class).getResultList();
            assertThat(audits).hasSize(1);
            assertThat(audits.get(0).getActorUserId()).isEqualTo(actor);
            assertThat(audits.get(0).getOrganisationId()).isEqualTo(org);
            assertThat(audits.get(0).getDetails()).containsOnlyKeys("applicationVersion");
            var rows = em.createQuery("select e from CommonOutboxEventEntity e", OutboxEventEntity.class).getResultList();
            assertThat(rows).hasSize(3);
            var transition = rows.stream().filter(e -> e.getEventType() == EventType.RESTAURANT_APPLICATION_STATUS_CHANGED)
                    .findFirst().orElseThrow();
            assertThat(transition.getIdempotencyKey()).isEqualTo("brand-application:" + id + ":3");
            var json = context.getBean(ObjectMapper.class).readTree(transition.getPayload());
            assertThat(json.get("status").asText()).isEqualTo("APPROVED");
            assertThat(json.get("organisationId").asText()).isEqualTo(org.toString());
            assertThat(json.has("bankAccountNumber")).isFalse();
            for (var row : rows) if (row.getEventType() == EventType.NOTIFICATION_DISPATCH) {
                var notification = context.getBean(ObjectMapper.class).readTree(row.getPayload());
                assertThat(notification.get("eventName").asText()).isEqualTo(NotificationTemplate.APPLICATION_APPROVED.name());
                assertThat(notification.get("templateParams").get(0).asText()).isEqualTo("Restaurant");
                assertThat(notification.get("payload").get("status").asText()).isEqualTo("APPROVED");
            }
            assertThat(metrics.get("applications.transitions").tags("type", "restaurant", "to", "APPROVED")
                    .counter().count()).isEqualTo(1);
        });
    }

    @Test void outerRollbackLeavesStateAuditOutboxAndCounterUnchanged() {
        runner().run(context -> {
            var em = context.getBean(EntityManager.class);
            var tx = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
            UUID id = UUID.randomUUID(); seed(em, tx, id);
            tx.executeWithoutResult(status -> {
                em.find(ApplicationState.class, id).status = ApplicationStatus.REJECTED;
                context.getBean(ApplicationEvents.class).delivery(id, ApplicationStatus.REJECTED,
                        "Please provide a legible document.", 4, Instant.now(), UUID.randomUUID(),
                        "ADMIN", AuditAction.APPLICATION_REJECTED, List.of(id));
                em.flush(); status.setRollbackOnly();
            });
            assertRolledBack(context.getBean(SimpleMeterRegistry.class), em, id);
        });
    }

    @Test void failedOutboxInsertRollsBackTheStateAndAudit() {
        runner().run(context -> {
            var em = context.getBean(EntityManager.class);
            var tx = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
            UUID id = UUID.randomUUID(); seed(em, tx, id);
            assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
                em.find(ApplicationState.class, id).status = ApplicationStatus.SUSPENDED;
                var events = context.getBean(ApplicationEvents.class);
                for (int duplicate = 0; duplicate < 2; duplicate++)
                    events.delivery(id, ApplicationStatus.SUSPENDED, "Review is required before taking new work.",
                            5, Instant.now(), UUID.randomUUID(), "ADMIN", AuditAction.APPLICATION_SUSPENDED, List.of(id));
                em.flush();
            })).isInstanceOf(RuntimeException.class);
            assertRolledBack(context.getBean(SimpleMeterRegistry.class), em, id);
        });
    }

    @Test void aDirectCallerWithoutTransactionCannotWriteAnything() {
        var outbox = mock(OutboxEventRepository.class);
        var audit = mock(AuditTrail.class);
        var notifications = mock(NotificationRouterService.class);
        var events = new ApplicationEvents(outbox, new ObjectMapper(), audit, notifications, new SimpleMeterRegistry());
        assertThatThrownBy(() -> events.delivery(UUID.randomUUID(), ApplicationStatus.SUBMITTED, null,
                1, Instant.now(), UUID.randomUUID(), "USER", AuditAction.APPLICATION_SUBMITTED, List.of()))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(outbox, audit, notifications);
    }

    @Test void aDatabaseFreeConsumerDoesNotInstantiateTheApplicationWriter() {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AuditConfiguration.class,
                OutboxConfiguration.class)).run(context -> assertThat(context).hasNotFailed()
                .doesNotHaveBean(ApplicationEvents.class).doesNotHaveBean(AuditTrail.class));
    }

    private static void seed(EntityManager em, TransactionTemplate tx, UUID id) {
        tx.executeWithoutResult(status -> em.persist(new ApplicationState(id)));
    }
    private static void assertRolledBack(SimpleMeterRegistry metrics, EntityManager em, UUID id) {
        assertThat(em.find(ApplicationState.class, id).status).isEqualTo(ApplicationStatus.IN_REVIEW);
        assertThat(em.createQuery("select count(e) from AuditEvent e", Long.class).getSingleResult()).isZero();
        assertThat(em.createQuery("select count(e) from CommonOutboxEventEntity e", Long.class).getSingleResult()).isZero();
        assertThat(metrics.find("applications.transitions").counter()).isNull();
    }
}
