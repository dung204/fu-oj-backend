package com.example.modules.auth.filters;

import com.example.base.dtos.ErrorResponseDTO;
import com.example.base.utils.AppRoutes;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.annotations.OptionalAuth;
import com.example.modules.auth.annotations.Public;
import com.example.modules.auth.enums.Role;
import com.example.modules.auth.exceptions.InvalidCredentialsException;
import com.example.modules.auth.exceptions.TokenInvalidatedException;
import com.example.modules.auth.services.JwtService;
import com.example.modules.users.entities.User;
import com.example.modules.users.repositories.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.lang.Collections;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UsersRepository usersRepository;
  private final RequestMappingHandlerMapping handlerMapping;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
    @NonNull HttpServletRequest request,
    @NonNull HttpServletResponse response,
    @NonNull FilterChain filterChain
  ) throws ServletException, IOException {
    final String currentRoute = request.getServletPath();
    final SecurityContext securityContext = SecurityContextHolder.getContext();
    final boolean isCurrentRoutePublic = isPublicAnnotatedRoute(request);
    final boolean isCurrentRouteOptionalAuth = isOptionalAuthAnnotatedRoute(request);

    if (AppRoutes.isWhitelistedRoute(currentRoute) || isCurrentRoutePublic) {
      bypassAuthentication(request, response, filterChain, securityContext);
      return;
    }

    final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      if (isCurrentRouteOptionalAuth) {
        bypassAuthentication(request, response, filterChain, securityContext);
        return;
      }

      sendUnauthorizedResponse(response, objectMapper);
      return;
    }

    try {
      final Jws<Claims> decodedToken = jwtService.verifyAccessToken(
        authorizationHeader.substring("Bearer ".length())
      );
      final Date tokenIssuedAt = decodedToken.getPayload().getIssuedAt();
      final String userId = decodedToken.getPayload().getSubject();

      if (jwtService.isTokenInvalidated(userId, tokenIssuedAt)) {
        throw new TokenInvalidatedException();
      }

      User user = usersRepository
        .findById(userId)
        .orElseThrow(() -> new InvalidCredentialsException());

      final String currentRole = decodedToken.getPayload().get("role", String.class);
      final List<String> allowRoles = getAllowRolesOfCurrentRoute(request)
        .stream()
        .map(Role::getValue)
        .collect(Collectors.toList());

      if (!allowRoles.isEmpty() && !allowRoles.contains(currentRole)) {
        sendUnauthorizedResponse(response, objectMapper);
        return;
      }

      securityContext.setAuthentication(
        new UsernamePasswordAuthenticationToken(
          user.getAccount().getEmail(),
          null,
          List.of(new SimpleGrantedAuthority(currentRole))
        )
      );
      response.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
      filterChain.doFilter(request, response);
    } catch (Exception e) {
      if (isCurrentRouteOptionalAuth) {
        bypassAuthentication(request, response, filterChain, securityContext);
        return;
      }

      sendUnauthorizedResponse(response, objectMapper);
    }
  }

  private boolean isPublicAnnotatedRoute(HttpServletRequest request) {
    try {
      final HandlerExecutionChain handlerChain = handlerMapping.getHandler(request);
      if (handlerChain != null && handlerChain.getHandler() instanceof HandlerMethod) {
        HandlerMethod handlerMethod = (HandlerMethod) handlerChain.getHandler();
        Method method = handlerMethod.getMethod();
        return method.isAnnotationPresent(Public.class);
      }
    } catch (Exception e) {
      return false;
    }
    return false;
  }

  private boolean isOptionalAuthAnnotatedRoute(HttpServletRequest request) {
    try {
      final HandlerExecutionChain handlerChain = handlerMapping.getHandler(request);
      if (handlerChain != null && handlerChain.getHandler() instanceof HandlerMethod) {
        HandlerMethod handlerMethod = (HandlerMethod) handlerChain.getHandler();
        Method method = handlerMethod.getMethod();
        return method.isAnnotationPresent(OptionalAuth.class);
      }
    } catch (Exception e) {
      return false;
    }
    return false;
  }

  private List<Role> getAllowRolesOfCurrentRoute(HttpServletRequest request) {
    try {
      final HandlerExecutionChain handlerChain = handlerMapping.getHandler(request);
      if (handlerChain != null && handlerChain.getHandler() instanceof HandlerMethod) {
        final HandlerMethod handlerMethod = (HandlerMethod) handlerChain.getHandler();
        final Method method = handlerMethod.getMethod();
        if (method.isAnnotationPresent(AllowRoles.class)) {
          final AllowRoles publicAnnotation = method.getAnnotation(AllowRoles.class);
          return List.of(publicAnnotation.value());
        }
      }
    } catch (Exception e) {
      return Collections.emptyList();
    }
    return Collections.emptyList();
  }

  private void sendUnauthorizedResponse(HttpServletResponse response, ObjectMapper objectMapper)
    throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(
      response.getOutputStream(),
      ErrorResponseDTO.builder()
        .status(HttpStatus.UNAUTHORIZED.value())
        .message("Unauthorized")
        .build()
    );
  }

  private void bypassAuthentication(
    @NonNull HttpServletRequest request,
    @NonNull HttpServletResponse response,
    @NonNull FilterChain filterChain,
    @NonNull SecurityContext securityContext
  ) throws IOException, ServletException {
    securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(null, null));
    filterChain.doFilter(request, response);
  }
}
