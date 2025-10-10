package com.example.modules.auth.controllers;

import static com.example.base.utils.AppRoutes.AUTH_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.base.BaseControllerIntegrationTest;
import com.example.base.dtos.ErrorResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.dtos.AuthTokenDTO;
import com.example.modules.auth.dtos.ChangePasswordRequestDTO;
import com.example.modules.auth.dtos.LoginRequestDTO;
import com.example.modules.auth.dtos.RefreshTokenRequestDTO;
import com.example.modules.auth.dtos.RegisterRequestDTO;
import com.example.modules.auth.services.JwtService;
import com.example.modules.users.entities.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class AuthControllerIntegrationTest extends BaseControllerIntegrationTest {

  @Autowired
  private JwtService jwtService;

  @Test
  void login_WhenValidCredentials_ShouldReturnCreatedResponse() {
    LoginRequestDTO loginRequest = LoginRequestDTO.builder()
      .email("email@example.com")
      .password("password@123456")
      .build();
    User user = usersRepository.findAll().get(0);

    ResponseEntity<SuccessResponseDTO<AuthTokenDTO>> response = restTemplate.exchange(
      AUTH_PREFIX + "/login",
      HttpMethod.POST,
      new HttpEntity<>(loginRequest),
      new ParameterizedTypeReference<SuccessResponseDTO<AuthTokenDTO>>() {}
    );

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(201, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    AuthTokenDTO tokenDTO = response.getBody().getData();
    assertNotNull(tokenDTO);

    assertNotNull(tokenDTO.getAccessToken());
    assertFalse(tokenDTO.getAccessToken().isEmpty());
    assertNotNull(tokenDTO.getRefreshToken());
    assertFalse(tokenDTO.getRefreshToken().isEmpty());

    assertEquals(3, tokenDTO.getAccessToken().split("\\.").length);
    assertEquals(3, tokenDTO.getRefreshToken().split("\\.").length);

    assertNotNull(tokenDTO.getUser());
    assertEquals(user.getId(), tokenDTO.getUser().getId());
    assertEquals(user.getAccount().getEmail(), tokenDTO.getUser().getEmail());
    assertEquals(user.getAccount().getRole().getValue(), tokenDTO.getUser().getRole());
  }

  @Test
  void login_WhenEmailIsEmpty_ShouldReturnBadRequest() throws Exception {
    // Given
    LoginRequestDTO loginRequest = LoginRequestDTO.builder()
      .email("")
      .password("password@123456")
      .build();

    ResponseEntity<SuccessResponseDTO<AuthTokenDTO>> response = restTemplate.exchange(
      AUTH_PREFIX + "/login",
      HttpMethod.POST,
      new HttpEntity<>(loginRequest),
      new ParameterizedTypeReference<SuccessResponseDTO<AuthTokenDTO>>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void login_WhenEmailIsInvalid_ShouldReturnBadRequest() throws Exception {
    LoginRequestDTO loginRequest = LoginRequestDTO.builder()
      .email("invalid-email")
      .password("password@123456")
      .build();

    ResponseEntity<SuccessResponseDTO<AuthTokenDTO>> response = restTemplate.exchange(
      AUTH_PREFIX + "/login",
      HttpMethod.POST,
      new HttpEntity<>(loginRequest),
      new ParameterizedTypeReference<SuccessResponseDTO<AuthTokenDTO>>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void login_WhenPasswordIsEmpty_ShouldReturnBadRequest() throws Exception {
    LoginRequestDTO loginRequest = LoginRequestDTO.builder()
      .email("test@example.com")
      .password("")
      .build();

    ResponseEntity<SuccessResponseDTO<AuthTokenDTO>> response = restTemplate.exchange(
      AUTH_PREFIX + "/login",
      HttpMethod.POST,
      new HttpEntity<>(loginRequest),
      new ParameterizedTypeReference<SuccessResponseDTO<AuthTokenDTO>>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void register_WhenValidCredentials_ShouldReturnCreatedResponse() throws Exception {
    RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
      .email("new-email@example.com")
      .password("password@123456")
      .build();

    ResponseEntity<SuccessResponseDTO<AuthTokenDTO>> response = restTemplate.exchange(
      AUTH_PREFIX + "/register",
      HttpMethod.POST,
      new HttpEntity<>(registerRequest),
      new ParameterizedTypeReference<SuccessResponseDTO<AuthTokenDTO>>() {}
    );

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(201, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    AuthTokenDTO tokenDTO = response.getBody().getData();
    assertNotNull(tokenDTO);

    assertNotNull(tokenDTO.getAccessToken());
    assertFalse(tokenDTO.getAccessToken().isEmpty());
    assertNotNull(tokenDTO.getRefreshToken());
    assertFalse(tokenDTO.getRefreshToken().isEmpty());

    assertEquals(3, tokenDTO.getAccessToken().split("\\.").length);
    assertEquals(3, tokenDTO.getRefreshToken().split("\\.").length);

    assertNotNull(tokenDTO.getUser());
    assertNotNull(tokenDTO.getUser().getId());
    assertEquals(registerRequest.getEmail(), tokenDTO.getUser().getEmail());
    assertEquals("USER", tokenDTO.getUser().getRole());
  }

  @Test
  void register_WhenEmailIsEmpty_ShouldReturnBadRequest() throws Exception {
    RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
      .email("")
      .password("password@123456")
      .build();

    ResponseEntity<SuccessResponseDTO<AuthTokenDTO>> response = restTemplate.exchange(
      AUTH_PREFIX + "/register",
      HttpMethod.POST,
      new HttpEntity<>(registerRequest),
      new ParameterizedTypeReference<SuccessResponseDTO<AuthTokenDTO>>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void register_WhenEmailIsInvalid_ShouldReturnBadRequest() throws Exception {
    RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
      .email("invalid-email")
      .password("password@123456")
      .build();

    ResponseEntity<SuccessResponseDTO<AuthTokenDTO>> response = restTemplate.exchange(
      AUTH_PREFIX + "/register",
      HttpMethod.POST,
      new HttpEntity<>(registerRequest),
      new ParameterizedTypeReference<SuccessResponseDTO<AuthTokenDTO>>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void register_WhenPasswordIsLessThan6Characters_ShouldReturnBadRequest() throws Exception {
    RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
      .email("new-email@example.com")
      .password("12345")
      .build();

    ResponseEntity<SuccessResponseDTO<AuthTokenDTO>> response = restTemplate.exchange(
      AUTH_PREFIX + "/register",
      HttpMethod.POST,
      new HttpEntity<>(registerRequest),
      new ParameterizedTypeReference<SuccessResponseDTO<AuthTokenDTO>>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void register_WhenPasswordIsEmpty_ShouldReturnBadRequest() throws Exception {
    RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
      .email("new-email@example.com")
      .password("")
      .build();

    ResponseEntity<SuccessResponseDTO<AuthTokenDTO>> response = restTemplate.exchange(
      AUTH_PREFIX + "/register",
      HttpMethod.POST,
      new HttpEntity<>(registerRequest),
      new ParameterizedTypeReference<SuccessResponseDTO<AuthTokenDTO>>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void refreshToken_WhenValidRefreshToken_ShouldReturnCreatedResponse() throws Exception {
    User user = getUser();
    String refreshToken = jwtService.generateRefreshToken(user);
    RefreshTokenRequestDTO refreshTokenRequest = RefreshTokenRequestDTO.builder()
      .refreshToken(refreshToken)
      .build();

    ResponseEntity<SuccessResponseDTO<AuthTokenDTO>> response = restTemplate.exchange(
      AUTH_PREFIX + "/refresh",
      HttpMethod.POST,
      new HttpEntity<>(refreshTokenRequest),
      new ParameterizedTypeReference<SuccessResponseDTO<AuthTokenDTO>>() {}
    );

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(201, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    AuthTokenDTO tokenDTO = response.getBody().getData();
    assertNotNull(tokenDTO);

    assertNotNull(tokenDTO.getAccessToken());
    assertFalse(tokenDTO.getAccessToken().isEmpty());
    assertNotNull(tokenDTO.getRefreshToken());
    assertFalse(tokenDTO.getRefreshToken().isEmpty());

    assertEquals(3, tokenDTO.getAccessToken().split("\\.").length);
    assertEquals(3, tokenDTO.getRefreshToken().split("\\.").length);

    assertNotNull(tokenDTO.getUser());
    assertNotNull(tokenDTO.getUser().getId());
    assertEquals(user.getAccount().getEmail(), tokenDTO.getUser().getEmail());
    assertEquals(user.getAccount().getRole().getValue(), tokenDTO.getUser().getRole());
  }

  @Test
  void refreshToken_WhenRefreshTokenIsEmpty_ShouldReturnBadRequest() throws Exception {
    RefreshTokenRequestDTO refreshTokenRequest = RefreshTokenRequestDTO.builder()
      .refreshToken("")
      .build();

    ResponseEntity<SuccessResponseDTO<AuthTokenDTO>> response = restTemplate.exchange(
      AUTH_PREFIX + "/refresh",
      HttpMethod.POST,
      new HttpEntity<>(refreshTokenRequest),
      new ParameterizedTypeReference<SuccessResponseDTO<AuthTokenDTO>>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void refreshToken_WhenRefreshTokenIsNull_ShouldReturnBadRequest() throws Exception {
    RefreshTokenRequestDTO refreshTokenRequest = RefreshTokenRequestDTO.builder()
      .refreshToken(null)
      .build();

    ResponseEntity<SuccessResponseDTO<AuthTokenDTO>> response = restTemplate.exchange(
      AUTH_PREFIX + "/refresh",
      HttpMethod.POST,
      new HttpEntity<>(refreshTokenRequest),
      new ParameterizedTypeReference<SuccessResponseDTO<AuthTokenDTO>>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void logout_WhenUserIsNotLoggedIn_ShouldReturnUnauthorizedResponse() throws Exception {
    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      AUTH_PREFIX + "/logout",
      HttpMethod.DELETE,
      HttpEntity.EMPTY,
      ErrorResponseDTO.class
    );

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void logout_WhenUserIsLoggedIn_ShouldReturnNoResponse() throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.put(HttpHeaders.AUTHORIZATION, List.of("Bearer " + accessToken));

    ResponseEntity<?> response = restTemplate.exchange(
      AUTH_PREFIX + "/logout",
      HttpMethod.DELETE,
      new HttpEntity<>(null, headers),
      Void.class
    );

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void changePassword_WhenUserIsNotLoggedIn_ShouldReturnUnauthorizedResponse() throws Exception {
    ChangePasswordRequestDTO changePasswordRequest = ChangePasswordRequestDTO.builder()
      .password("password@123456")
      .newPassword("newPassword123")
      .build();

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      AUTH_PREFIX + "/logout",
      HttpMethod.PATCH,
      new HttpEntity<>(changePasswordRequest),
      ErrorResponseDTO.class
    );

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void changePassword_WhenUserIsLoggedInAndValidRequest_ShouldReturnNoContent() throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.put(HttpHeaders.AUTHORIZATION, List.of("Bearer " + accessToken));

    ChangePasswordRequestDTO changePasswordRequest = ChangePasswordRequestDTO.builder()
      .password("password@123456")
      .newPassword("newPassword123")
      .build();

    ResponseEntity<Void> response = restTemplate.exchange(
      AUTH_PREFIX + "/password",
      HttpMethod.PATCH,
      new HttpEntity<>(changePasswordRequest, headers),
      Void.class
    );

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
  }

  @Test
  void changePassword_WhenNewPasswordIsEmpty_ShouldReturnBadRequest() throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.put(HttpHeaders.AUTHORIZATION, List.of("Bearer " + accessToken));

    ChangePasswordRequestDTO changePasswordRequest = ChangePasswordRequestDTO.builder()
      .password("password@123456")
      .build();

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      AUTH_PREFIX + "/password",
      HttpMethod.PATCH,
      new HttpEntity<>(changePasswordRequest, headers),
      ErrorResponseDTO.class
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void changePassword_WhenNewPasswordIsLessThan6Characters_ShouldReturnBadRequest()
    throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.put(HttpHeaders.AUTHORIZATION, List.of("Bearer " + accessToken));

    ChangePasswordRequestDTO changePasswordRequest = ChangePasswordRequestDTO.builder()
      .password("password@123456")
      .newPassword("12345")
      .build();

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      AUTH_PREFIX + "/password",
      HttpMethod.PATCH,
      new HttpEntity<>(changePasswordRequest, headers),
      ErrorResponseDTO.class
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }
}
