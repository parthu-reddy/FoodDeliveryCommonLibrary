package com.fooddelivery.common.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

@AnalyzeClasses(packages = "com.fooddelivery")
public class OutboxArchitectureTest {

    @ArchTest
    static final ArchRule controllers_should_not_inject_kafka_template =
            ArchRuleDefinition.noClasses()
                    .that().haveSimpleNameEndingWith("Controller")
                    .should().dependOnClassesThat().haveFullyQualifiedName(KafkaTemplate.class.getName())
                    .allowEmptyShould(true)
                    .because("Controllers must not publish events directly. They should use the Outbox pattern via Services.");

    @ArchTest
    static final ArchRule services_modifying_outbox_must_be_transactional =
            ArchRuleDefinition.methods()
                    .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("Service")
                    .and().haveNameMatching(".*save.*Outbox.*")
                    .should().beAnnotatedWith(Transactional.class)
                    .allowEmptyShould(true)
                    .because("Any service method saving to the outbox MUST be transactional to prevent dual-write vulnerabilities.");

    @ArchTest
    static final ArchRule methods_publishing_kafka_events_must_be_transactional =
            ArchRuleDefinition.methods()
                    .that().haveNameMatching(".*publish.*")
                    .and().areDeclaredInClassesThat().haveSimpleNameEndingWith("Service")
                    .should().beAnnotatedWith(Transactional.class)
                    .allowEmptyShould(true)
                    .because("Any method that publishes a Kafka event MUST be annotated with @Transactional.");

    @ArchTest
    static final ArchRule controllers_should_not_inject_redis_template =
            ArchRuleDefinition.noClasses()
                    .that().haveSimpleNameEndingWith("Controller")
                    .should().dependOnClassesThat().haveFullyQualifiedName(RedisTemplate.class.getName())
                    .allowEmptyShould(true)
                    .because("Controllers must not directly modify the cache. Cache invalidation should be event-driven via Kafka Listeners.");

}
