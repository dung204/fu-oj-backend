package com.example.base.configs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {

  @Bean
  ObjectMapper objectMapper() {
    // Create and configure the ObjectMapper
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JsonNullableModule());
    mapper.registerModule(new JavaTimeModule());

    // avoid Jackson escape
    mapper.configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), false);
    mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

    // ignore unknown properties in JSON input
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    return mapper;
  }
}
