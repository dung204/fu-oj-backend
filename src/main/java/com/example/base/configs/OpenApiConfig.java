package com.example.base.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
  info = @Info(
    title = "Spring REST API Starter Template",
    version = "1.0",
    description = "A starter template for building REST APIs with Spring Boot",
    contact = @Contact(
      name = "Ho Anh Dung (a.k.a Mantrilogix)",
      email = "acezombiev4@gmail.com",
      url = "https://github.com/dung204"
    )
  ),
  security = { @SecurityRequirement(name = "JWT") }
)
@SecurityScheme(
  name = "JWT",
  description = "Enter JWT authentication (access) token",
  bearerFormat = "Bearer",
  in = SecuritySchemeIn.HEADER,
  type = SecuritySchemeType.HTTP,
  scheme = "bearer"
)
@Configuration
public class OpenApiConfig {}
