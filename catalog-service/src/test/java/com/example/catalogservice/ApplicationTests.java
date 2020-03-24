package com.example.catalogservice;

import com.example.catalogservice.common.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTests extends AbstractIntegrationTest {

  @Test
  void contextLoads() {
    assertThat(POSTGRES_SQL_CONTAINER.isRunning()).isTrue();
  }

}
