package com.example.base.utils;

import java.util.List;
import org.springframework.util.AntPathMatcher;

/**
 * Utility class that defines API route constants and provides route matching functionality.
 *
 * <p>This class contains all the API endpoint prefixes used throughout the application
 * and maintains a whitelist of routes that don't require authentication or special handling.
 * It uses Spring's AntPathMatcher for pattern-based route matching.</p>
 *
 * <p>The class follows a hierarchical structure with a base API prefix and specific
 * prefixes for different resource types (auth, users, posts).</p>
 *
 * <h3>Whitelisted Routes:</h3>
 * The following routes are whitelisted and typically bypass authentication:
 * <ul>
 *   <li>API documentation endpoints</li>
 *   <li>Swagger UI resources</li>
 *   <li>Static assets (favicon, index page)</li>
 * </ul>
 *
 */
public final class AppRoutes {

  private static final AntPathMatcher pathMatcher = new AntPathMatcher();

  public static final String API_PREFIX = "/api/v1";

  public static final String AUTH_PREFIX = API_PREFIX + "/auth";
  public static final String USER_PREFIX = API_PREFIX + "/users";
  public static final String ME_PREFIX = API_PREFIX + "/me";
  public static final String POSTS_PREFIX = API_PREFIX + "/posts";

  public static final List<String> whitelistedRoutes = List.of(
    API_PREFIX + "/docs/**",
    API_PREFIX + "/swagger-ui/**",
    "/api-docs/**",
    "/favicon.ico",
    "/index.html",
    "/swagger-ui/**"
  );

  public static boolean isWhitelistedRoute(String path) {
    return whitelistedRoutes.stream().anyMatch(route -> pathMatcher.match(route, path));
  }
}
