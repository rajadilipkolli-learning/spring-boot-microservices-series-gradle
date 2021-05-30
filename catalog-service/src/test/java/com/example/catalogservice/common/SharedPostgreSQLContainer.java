package com.example.catalogservice.common;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public class SharedPostgreSQLContainer {

  @Container
  protected static final PostgreSQLContainer<?> POSTGRES_SQL_CONTAINER =
          new PostgreSQLContainer<>("postgres:latest")
                  .withDatabaseName("integration-tests-db")
                  .withUsername("username")
                  .withPassword("password");

  static {
    POSTGRES_SQL_CONTAINER.start();
  }

  @DynamicPropertySource
  static void setPostgreSQLContainer(DynamicPropertyRegistry propertyRegistry) {
    propertyRegistry.add("spring.datasource.url", POSTGRES_SQL_CONTAINER::getJdbcUrl);
    propertyRegistry.add("spring.datasource.username", POSTGRES_SQL_CONTAINER::getUsername);
    propertyRegistry.add("spring.datasource.password", POSTGRES_SQL_CONTAINER::getPassword);
    propertyRegistry.add("spring.liquibase.url", POSTGRES_SQL_CONTAINER::getJdbcUrl);
    propertyRegistry.add("spring.liquibase.user", POSTGRES_SQL_CONTAINER::getUsername);
    propertyRegistry.add("spring.liquibase.password", POSTGRES_SQL_CONTAINER::getPassword);
  }
}
