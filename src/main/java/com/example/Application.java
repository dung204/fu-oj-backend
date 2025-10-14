package com.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class Application {

  private final Environment environment;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void logSwaggerUrl() {
    String port = environment.getProperty("server.port", "8080");
    String swaggerPath = environment.getProperty("springdoc.swagger-ui.path", "/swagger-ui.html");

    String swaggerUrl = String.format("http://localhost:%s%s", port, swaggerPath);

    log.info("Swagger UI is available at {}", swaggerUrl);
  }
}
