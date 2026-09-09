package com.fooddelivery.common.test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
/**
 * Integration-test base. Everything runs in process: H2 in PostgreSQL mode for the database, and
 * EmbeddedKafka or a mock where a broker is needed.
 *
 * <p>Testcontainers are excluded by project rule -- see
 * {@code CodingPracticesAcrossAllServices/05_DataLayer/jpa-and-flyway.md}. This class still imported
 * {@code PostgreSQLContainer}, {@code KafkaContainer} and {@code GenericContainer} without using any
 * of them, which is how a Testcontainers-based test came to be written against it.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // Pointing to H2 instead of Postgres for CI/CD compatibility
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
        registry.add("spring.jpa.database-platform", () -> "com.fooddelivery.contract.CustomH2Dialect");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        // Keep Redis/Kafka to localhost or mocked
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> 6379);

        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
        registry.add("kafka.bootstrap-servers", () -> "localhost:9092");
    }
}


