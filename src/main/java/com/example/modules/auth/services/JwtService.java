package com.example.modules.auth.services;

import com.example.modules.auth.exceptions.InvalidCredentialsException;
import com.example.modules.redis.services.RedisService;
import com.example.modules.users.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

  @Value("${jwt.access.secret}")
  private String ACCESS_SECRET;

  @Value("${jwt.refresh.secret}")
  private String REFRESH_SECRET;

  @Value("${jwt.access.expiration}")
  private Long ACCESS_EXPIRATION;

  @Value("${jwt.refresh.expiration}")
  private Long REFRESH_EXPIRATION;

  private final RedisService redisService;

  public boolean isTokenInvalidated(String userId, Date tokenIssuedAt) {
    String key = "user:%s:tokens:invalidated_before".formatted(userId);
    Instant invalidatedBefore = redisService.get(key, Instant.class);

    if (invalidatedBefore == null) {
      return false;
    }

    return tokenIssuedAt.toInstant().isBefore(invalidatedBefore);
  }

  public String generateAccessToken(User user) {
    Instant currentInstant = Instant.now();
    Date issuedAt = Date.from(currentInstant);
    Date expiration = Date.from(currentInstant.plusSeconds(ACCESS_EXPIRATION));

    return Jwts.builder()
      .subject(user.getId())
      .claim("role", user.getAccount().getRole())
      .issuedAt(issuedAt)
      .expiration(expiration)
      .signWith(getSecretKeyFromString(ACCESS_SECRET))
      .compact();
  }

  public String generateRefreshToken(User user) {
    Instant currentInstant = Instant.now();
    Date issuedAt = Date.from(currentInstant);
    Date expiration = Date.from(currentInstant.plusSeconds(REFRESH_EXPIRATION));

    return Jwts.builder()
      .subject(user.getId())
      .issuedAt(issuedAt)
      .expiration(expiration)
      .signWith(getSecretKeyFromString(REFRESH_SECRET))
      .compact();
  }

  public Jws<Claims> verifyAccessToken(String token) {
    try {
      return Jwts.parser()
        .verifyWith(getSecretKeyFromString(ACCESS_SECRET))
        .build()
        .parseSignedClaims(token);
    } catch (Exception e) {
      throw new InvalidCredentialsException(e.getMessage());
    }
  }

  public Jws<Claims> verifyRefreshToken(String token) {
    return Jwts.parser()
      .verifyWith(getSecretKeyFromString(REFRESH_SECRET))
      .build()
      .parseSignedClaims(token);
  }

  private SecretKey getSecretKeyFromString(String secret) {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
