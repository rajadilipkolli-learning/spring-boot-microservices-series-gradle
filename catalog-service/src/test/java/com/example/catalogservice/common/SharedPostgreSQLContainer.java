package com.example.catalogservice.common;

import org.testcontainers.containers.PostgreSQLContainer;

public class SharedPostgreSQLContainer
    extends PostgreSQLContainer<SharedPostgreSQLContainer> {
  private static final String IMAGE_VERSION = "postgres:12.3";

  private static SharedPostgreSQLContainer container;

  private SharedPostgreSQLContainer() {
    super(IMAGE_VERSION);
  }

  public static SharedPostgreSQLContainer getInstance() {
    if (container == null) {
      container = new SharedPostgreSQLContainer();
    }
    return container;
  }

  @Override
  public void start() {
    super.start();
    System.setProperty("spring.datasource.url", container.getJdbcUrl());
    System.setProperty("spring.datasource.username", container.getUsername());
    System.setProperty("spring.datasource.password", container.getPassword());
  }

  @Override
  public void stop() {
    //do nothing, JVM handles shut down
  }
}
