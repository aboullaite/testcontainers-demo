package me.aboullaite.testcontainersdemo;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
public abstract class AbstractIntegrationTest {

  // PostgreSQL container
  static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
      DockerImageName.parse("postgres:17-alpine"))
      .withDatabaseName("test-db")
      .withUsername("test-user")
      .withPassword("test-password");

  // Redis container
  static final GenericContainer<?> redisContainer = new GenericContainer<>(
      DockerImageName.parse("redis:7-alpine"))
      .withExposedPorts(6379);

  // Kafka container
  static final KafkaContainer kafkaContainer = new KafkaContainer(
      DockerImageName.parse("confluentinc/cp-kafka:7.9.0"));

  static {
    // Start all containers
    postgreSQLContainer.start();
    redisContainer.start();
    kafkaContainer.start();
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    // PostgreSQL properties
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

    // Redis properties
    registry.add("spring.data.redis.host", redisContainer::getHost);
    registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);

    // Kafka properties
    registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
  }
}