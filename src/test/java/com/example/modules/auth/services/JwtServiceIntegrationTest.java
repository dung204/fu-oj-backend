package com.example.modules.auth.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.example.base.BaseServiceIntegrationTest;
import com.example.modules.auth.exceptions.InvalidCredentialsException;
import com.example.modules.redis.services.RedisService;
import com.example.modules.users.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class JwtServiceIntegrationTest extends BaseServiceIntegrationTest {

  @Autowired
  private JwtService jwtService;

  @Autowired
  private RedisService redisService;

  @Test
  void testIsTokenInvalidated_WhenNoInvalidationExists_ShouldReturnFalse() {
    User user = getUser();
    Date tokenIssuedAt = new Date();

    boolean result = jwtService.isTokenInvalidated(user.getId(), tokenIssuedAt);
    assertFalse(result);
  }

  @Test
  void isTokenInvalidated_WhenTokenIssuedBeforeInvalidation_ShouldReturnTrue() {
    User user = getUser();
    String key = "user:%s:tokens:invalidated_before".formatted(user.getId());
    Instant invalidatedBefore = Instant.now();

    redisService.set(key, invalidatedBefore);

    Date tokenIssuedAt = Date.from(invalidatedBefore.minusSeconds(10));
    boolean result = jwtService.isTokenInvalidated(user.getId(), tokenIssuedAt);

    assertTrue(result);
  }

  @Test
  void isTokenInvalidated_WhenTokenIssuedAfterInvalidation_ShouldReturnFalse() {
    User user = getUser();
    String key = "user:%s:tokens:invalidated_before".formatted(user.getId());
    Instant invalidatedBefore = Instant.now();

    redisService.set(key, invalidatedBefore);

    Date tokenIssuedAt = Date.from(invalidatedBefore.plusSeconds(10));
    boolean result = jwtService.isTokenInvalidated(user.getId(), tokenIssuedAt);

    assertFalse(result);
  }

  @Test
  void generateAccessToken_ShouldProduceValidJwtWithCorrectClaims() {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    assertNotNull(accessToken);

    Jws<Claims> parsed = jwtService.verifyAccessToken(accessToken);
    Claims claims = parsed.getPayload();

    assertEquals(user.getId(), claims.getSubject());
    assertEquals(user.getAccount().getRole().getValue(), claims.get("role"));
    assertNotNull(claims.getIssuedAt());
    assertNotNull(claims.getExpiration());
    assertNotNull(claims.getExpiration().after(claims.getIssuedAt()));
  }

  @Test
  void generateRefreshToken_ShouldProduceValidJwtWithCorrectClaims() {
    User user = getUser();
    String accessToken = jwtService.generateRefreshToken(user);

    assertNotNull(accessToken);

    Jws<Claims> parsed = jwtService.verifyRefreshToken(accessToken);
    Claims claims = parsed.getPayload();

    assertEquals(user.getId(), claims.getSubject());
    assertNotNull(claims.getIssuedAt());
    assertNotNull(claims.getExpiration());
    assertNotNull(claims.getExpiration().after(claims.getIssuedAt()));
  }

  @Test
  void verifyAccessToken_WhenAccessTokenIsInvalid_ShouldThrowInvalidCredentialsException() {
    String invalidToken = "invalid.token.value";
    assertThrows(InvalidCredentialsException.class, () ->
      jwtService.verifyAccessToken(invalidToken)
    );
  }

  @Test
  void verifyRefreshToken_WhenRefreshTokenIsInvalid_ShouldThrowException() {
    String invalidToken = "invalid.token.value";
    assertThrows(Exception.class, () -> jwtService.verifyRefreshToken(invalidToken));
  }
}
