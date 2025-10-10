package com.example.modules.auth.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.base.BaseServiceTest;
import com.example.modules.auth.exceptions.InvalidCredentialsException;
import com.example.modules.redis.services.RedisService;
import com.example.modules.users.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

public class JwtServiceTest extends BaseServiceTest {

  @Mock
  private RedisService redisService;

  @InjectMocks
  private JwtService jwtService;

  private static final String TEST_ACCESS_SECRET =
    "42d9aab082b09baabc0d90152d3b4d62eb70e8d747760a9fddda3f60a9596156f7c3615e83a826859d3fedb6846a38eafa457bbcd37860564717154850941776";
  private static final String TEST_REFRESH_SECRET =
    "b95b819f39de3074179c9c878943447cfd34b2f8525b8fca5441380d94f69cdb804f3bd56a5be33e688e86bd2bc667a3efefe8ccc56a6a52b36a2e2b39044bfd";
  private static final Long TEST_ACCESS_EXPIRATION = 3600L;
  private static final Long TEST_REFRESH_EXPIRATION = 7200L;

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(jwtService, "ACCESS_SECRET", TEST_ACCESS_SECRET);
    ReflectionTestUtils.setField(jwtService, "REFRESH_SECRET", TEST_REFRESH_SECRET);
    ReflectionTestUtils.setField(jwtService, "ACCESS_EXPIRATION", TEST_ACCESS_EXPIRATION);
    ReflectionTestUtils.setField(jwtService, "REFRESH_EXPIRATION", TEST_REFRESH_EXPIRATION);
  }

  @Test
  void testIsTokenInvalidated_WhenInvalidatedBeforeIsNull_ShouldReturnFalse() {
    String userId = "user123";
    Date tokenIssuedAt = new Date();
    String key = "user:%s:tokens:invalidated_before".formatted(userId);

    when(redisService.get(key, Instant.class)).thenReturn(null);

    boolean result = jwtService.isTokenInvalidated(userId, tokenIssuedAt);

    assertFalse(result);
  }

  @Test
  void testIsTokenInvalidated_WhenTokenIssuedAfterInvalidatedBefore_ShouldReturnFalse() {
    String userId = "user123";
    Instant invalidatedBefore = Instant.now().minusSeconds(60);
    Date tokenIssuedAt = Date.from(Instant.now());
    String key = "user:%s:tokens:invalidated_before".formatted(userId);

    when(redisService.get(key, Instant.class)).thenReturn(invalidatedBefore);

    boolean result = jwtService.isTokenInvalidated(userId, tokenIssuedAt);

    assertFalse(result);
  }

  @Test
  void testIsTokenInvalidated_WhenTokenIssuedBeforeInvalidatedBefore_ShouldReturnTrue() {
    String userId = "user123";
    Instant invalidatedBefore = Instant.now();
    Date tokenIssuedAt = Date.from(invalidatedBefore.minusSeconds(60));
    String key = "user:%s:tokens:invalidated_before".formatted(userId);

    when(redisService.get(key, Instant.class)).thenReturn(invalidatedBefore);

    boolean result = jwtService.isTokenInvalidated(userId, tokenIssuedAt);

    assertTrue(result);
  }

  @Test
  void testGenerateAccessToken_ShouldContainCorrectClaims() {
    User mockUser = getMockUser();

    String token = jwtService.generateAccessToken(mockUser);

    Jws<Claims> parsed = jwtService.verifyAccessToken(token);
    Claims claims = parsed.getPayload();

    assertEquals(mockUser.getId(), claims.getSubject());
    assertEquals(mockUser.getAccount().getRole().getValue(), claims.get("role"));
    assertNotNull(claims.getIssuedAt());
    assertNotNull(claims.getExpiration());
    assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
  }

  @Test
  void testGenerateAccessToken_ShouldHaveValidExpiration() {
    User mockUser = getMockUser();
    String token = jwtService.generateAccessToken(mockUser);

    Jws<Claims> parsed = jwtService.verifyAccessToken(token);
    Claims claims = parsed.getPayload();

    long expectedExpiration = claims
      .getIssuedAt()
      .toInstant()
      .plusSeconds(TEST_ACCESS_EXPIRATION)
      .getEpochSecond();
    long actualExpiration = claims.getExpiration().toInstant().getEpochSecond();

    assertEquals(expectedExpiration, actualExpiration, 2); // allow 2 seconds drift
  }

  @Test
  void testGenerateRefreshToken_ShouldContainCorrectClaims() {
    User mockUser = getMockUser();

    String token = jwtService.generateRefreshToken(mockUser);

    Jws<Claims> parsed = jwtService.verifyRefreshToken(token);
    Claims claims = parsed.getPayload();

    assertEquals(mockUser.getId(), claims.getSubject());
    assertNotNull(claims.getIssuedAt());
    assertNotNull(claims.getExpiration());
    assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
  }

  @Test
  void testGenerateRefreshToken_ShouldHaveValidExpiration() {
    User mockUser = getMockUser();
    String token = jwtService.generateRefreshToken(mockUser);

    Jws<Claims> parsed = jwtService.verifyRefreshToken(token);
    Claims claims = parsed.getPayload();

    long expectedExpiration = claims
      .getIssuedAt()
      .toInstant()
      .plusSeconds(TEST_REFRESH_EXPIRATION)
      .getEpochSecond();
    long actualExpiration = claims.getExpiration().toInstant().getEpochSecond();

    assertEquals(expectedExpiration, actualExpiration, 2); // allow 2 seconds drift
  }

  @Test
  void testVerifyAccessToken_WithValidToken_ShouldReturnClaims() {
    User mockUser = getMockUser();
    String token = jwtService.generateAccessToken(mockUser);

    Jws<Claims> parsed = jwtService.verifyAccessToken(token);
    Claims claims = parsed.getPayload();

    assertEquals(mockUser.getId(), claims.getSubject());
    assertEquals(mockUser.getAccount().getRole().getValue(), claims.get("role"));
    assertNotNull(claims.getIssuedAt());
    assertNotNull(claims.getExpiration());
  }

  @Test
  void testVerifyAccessToken_WithInvalidToken_ShouldThrowInvalidCredentialsException() {
    String invalidToken = "invalid.token.value";

    Exception exception = assertThrows(InvalidCredentialsException.class, () ->
      jwtService.verifyAccessToken(invalidToken)
    );

    assertNotNull(exception.getMessage());
  }

  @Test
  void testVerifyAccessToken_WithExpiredToken_ShouldThrowInvalidCredentialsException() {
    User mockUser = getMockUser();
    // Create a token with expiration in the past
    ReflectionTestUtils.setField(jwtService, "ACCESS_EXPIRATION", -10L); // expired 10 seconds ago
    String expiredToken = jwtService.generateAccessToken(mockUser);

    ReflectionTestUtils.setField(jwtService, "ACCESS_EXPIRATION", TEST_ACCESS_EXPIRATION); // restore

    Exception exception = assertThrows(InvalidCredentialsException.class, () ->
      jwtService.verifyAccessToken(expiredToken)
    );

    assertNotNull(exception.getMessage());
  }

  @Test
  void testVerifyRefreshToken_WithValidToken_ShouldReturnClaims() {
    User mockUser = getMockUser();
    String token = jwtService.generateRefreshToken(mockUser);

    Jws<Claims> parsed = jwtService.verifyRefreshToken(token);
    Claims claims = parsed.getPayload();

    assertEquals(mockUser.getId(), claims.getSubject());
    assertNotNull(claims.getIssuedAt());
    assertNotNull(claims.getExpiration());
    assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
  }

  @Test
  void testVerifyRefreshToken_WithExpiredToken_ShouldThrowException() {
    User mockUser = getMockUser();
    ReflectionTestUtils.setField(jwtService, "REFRESH_EXPIRATION", -10L); // expired 10 seconds ago
    String expiredToken = jwtService.generateRefreshToken(mockUser);
    ReflectionTestUtils.setField(jwtService, "REFRESH_EXPIRATION", TEST_REFRESH_EXPIRATION); // restore

    assertThrows(Exception.class, () -> jwtService.verifyRefreshToken(expiredToken));
  }

  @Test
  void testVerifyRefreshToken_WithInvalidToken_ShouldThrowException() {
    String invalidToken = "invalid.token.value";
    assertThrows(Exception.class, () -> jwtService.verifyRefreshToken(invalidToken));
  }
}
