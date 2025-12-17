package com.example.base.configs;

import com.example.modules.auth.resolvers.CurrentUserArgumentResolver;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reactor.netty.http.client.HttpClient;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final CurrentUserArgumentResolver currentUserArgumentResolver;

  @Override
  public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(currentUserArgumentResolver);
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**").allowedOrigins("*").allowedMethods("*").allowedHeaders("*");
  }

  @Bean
  public WebClient webClient(WebClient.Builder builder) {
    // Tăng buffer size lên 10MB để xử lý response lớn từ Judge0 (TLE với output lớn)
    ExchangeStrategies strategies = ExchangeStrategies.builder()
      .codecs(
        configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) // 10MB
      )
      .build();

    // Cấu hình timeout cho HTTP client
    HttpClient httpClient = HttpClient.create()
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000) // Connection timeout: 30s
      .responseTimeout(Duration.ofSeconds(60)) // Response timeout: 60s
      .doOnConnected(conn ->
        conn
          .addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
          .addHandlerLast(new WriteTimeoutHandler(60, TimeUnit.SECONDS))
      );

    return builder
      .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
      .exchangeStrategies(strategies)
      .clientConnector(new ReactorClientHttpConnector(httpClient))
      .build();
  }
}
