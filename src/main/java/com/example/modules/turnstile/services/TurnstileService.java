package com.example.modules.turnstile.services;

import com.example.modules.turnstile.dtos.TurnstileVerifyResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class TurnstileService {

  private final WebClient webClient = WebClient.create();

  @Value("${cloudflare.turnstile.secret-key}")
  private String secretKey;

  @Value("${cloudflare.turnstile.verify-url}")
  private String verifyUrl;

  public boolean verifyToken(String token) {
    try {
      TurnstileVerifyResponseDto response = webClient
        .post()
        .uri(verifyUrl)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData("secret", secretKey).with("response", token))
        .retrieve()
        .bodyToMono(TurnstileVerifyResponseDto.class)
        .onErrorResume(e -> {
          log.error("Error verifying Turnstile token", e);
          return Mono.empty();
        })
        .block();

      if (response == null) {
        log.warn("No response from Cloudflare Turnstile API");
        return false;
      }

      if (!response.isSuccess()) {
        log.warn("Turnstile verification failed: {}", (Object) response.getErrorCodes());
        return false;
      }

      log.debug("✅ Turnstile verification success for hostname: {}", response.getHostname());
      return true;
    } catch (Exception e) {
      log.error("Unexpected error verifying Turnstile token", e);
      return false;
    }
  }
}
