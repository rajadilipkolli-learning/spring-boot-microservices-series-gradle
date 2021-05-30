package com.example.catalogservice.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.zalando.problem.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

import javax.validation.constraints.NotNull;
import java.util.stream.Stream;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public abstract class AbstractIntegrationTest extends SharedPostgreSQLContainer{

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected ObjectMapper objectMapper;

  @BeforeAll
  void setUp() {
    objectMapper.registerModule(new ProblemModule());
    objectMapper.registerModule(new ConstraintViolationProblemModule());
  }

  public static class Initializer implements ApplicationContextInitializer {

    public static GenericContainer eurekaServer =
            new GenericContainer("springcloud/eureka").withExposedPorts(8761);

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext configurableApplicationContext) {

      Startables.deepStart(Stream.of(eurekaServer)).join();

      TestPropertyValues
              .of("eureka.client.serviceUrl.defaultZone=http://localhost:"
                      + eurekaServer.getFirstMappedPort().toString()
                      + "/eureka")
              .applyTo(configurableApplicationContext);
    }
  }

}
