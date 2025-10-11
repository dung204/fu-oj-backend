package com.example.base.aspect;

import com.example.modules.turnstile.services.TurnstileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TurnstileAspect {

  private final TurnstileService turnstileService;
  private final HttpServletRequest request;

  // thằng này có vẻ giống interceptor trong nestjs
  @Before("@annotation(com.example.base.annotations.VerifyTurnstile)") // this method will run before any method annotated with @VerifyTurnstile
  public void verifyTurnstileToken() {
    String token = request.getHeader("cf-turnstile-response");
    if (token == null || token.isEmpty()) {
      log.warn("Missing Turnstile token in request header");
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Missing Turnstile token");
    }

    boolean valid = turnstileService.verifyToken(token);
    if (!valid) {
      log.warn("Invalid Turnstile token: {}", token);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Turnstile token");
    }

    log.info("Turnstile token verified successfully");
  }
}
