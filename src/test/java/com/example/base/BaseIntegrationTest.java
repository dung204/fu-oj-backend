package com.example.base;

import com.example.modules.auth.entities.Account;
import com.example.modules.auth.repositories.AccountsRepository;
import com.example.modules.users.entities.User;
import com.example.modules.users.repositories.UsersRepository;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@Tag("integration")
public class BaseIntegrationTest {

  @SuppressWarnings("resource")
  @ServiceConnection
  protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
    .withUsername("postgres")
    .withPassword("postgres")
    .withInitScript(null);

  protected static RedisContainer redis = new RedisContainer("redis:7.0-alpine");

  protected static MinIOContainer minio = new MinIOContainer("minio/minio:latest");

  @DynamicPropertySource
  static void populateProperties(DynamicPropertyRegistry registry) {
    registry.add("minio.endpoint", minio::getS3URL);
    registry.add("minio.access-key", minio::getUserName);
    registry.add("minio.secret-key", minio::getPassword);
    registry.add("minio.bucket", () -> "spring-rest-api-bucket");

    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getRedisPort);
  }

  @BeforeAll
  static void startTestContainers() {
    postgres.start();
    redis.start();
    minio.start();
  }

  @Autowired
  protected AccountsRepository accountsRepository;

  @Autowired
  protected UsersRepository usersRepository;

  @Autowired
  protected PasswordEncoder passwordEncoder;

  protected Account getAccount() {
    return accountsRepository.findAll().get(0);
  }

  protected User getUser() {
    return usersRepository.findAll().get(0);
  }
}
