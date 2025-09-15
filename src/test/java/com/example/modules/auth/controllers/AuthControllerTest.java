package com.example.modules.auth.controllers;

import static com.example.base.utils.AppRoutes.AUTH_PREFIX;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.base.BaseControllerTest;
import com.example.modules.auth.dtos.AuthTokenDTO;
import com.example.modules.auth.dtos.ChangePasswordRequestDTO;
import com.example.modules.auth.dtos.LoginRequestDTO;
import com.example.modules.auth.dtos.RefreshTokenRequestDTO;
import com.example.modules.auth.dtos.RegisterRequestDTO;
import com.example.modules.auth.enums.Role;
import com.example.modules.users.dtos.UserProfileDTO;
import com.example.modules.users.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

@WebMvcTest(AuthController.class)
public class AuthControllerTest extends BaseControllerTest {

  private static final AuthTokenDTO mockSuccessResponse = AuthTokenDTO.builder()
    .accessToken("mock-access-token")
    .refreshToken("mock-refresh-token")
    .user(
      UserProfileDTO.builder()
        .id("mock-id")
        .email("email@example.com")
        .firstName("John")
        .lastName("Doe")
        .role(Role.USER.getValue())
        .build()
    )
    .build();

  @Test
  void login_WhenValidCredentials_ShouldReturnCreatedResponse() throws Exception {
    LoginRequestDTO loginRequest = LoginRequestDTO.builder()
      .email("email@example.com")
      .password("password@123456")
      .build();

    when(authService.login(any(LoginRequestDTO.class))).thenReturn(mockSuccessResponse);

    mockMvc
      .perform(
        post(AUTH_PREFIX + "/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest))
      )
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.status").value(201))
      .andExpect(jsonPath("$.message").isString())
      .andExpect(jsonPath("$.data.accessToken").value(mockSuccessResponse.getAccessToken()))
      .andExpect(jsonPath("$.data.refreshToken").value(mockSuccessResponse.getRefreshToken()))
      .andExpect(jsonPath("$.data.user.id").value(mockSuccessResponse.getUser().getId()))
      .andExpect(jsonPath("$.data.user.email").value(mockSuccessResponse.getUser().getEmail()))
      .andExpect(
        jsonPath("$.data.user.firstName").value(mockSuccessResponse.getUser().getFirstName())
      )
      .andExpect(
        jsonPath("$.data.user.lastName").value(mockSuccessResponse.getUser().getLastName())
      )
      .andExpect(jsonPath("$.data.user.role").value(mockSuccessResponse.getUser().getRole()));
  }

  @Test
  void login_WhenEmailIsEmpty_ShouldReturnBadRequest() throws Exception {
    // Given
    LoginRequestDTO loginRequest = LoginRequestDTO.builder()
      .email("")
      .password("password@123456")
      .build();

    when(authService.login(any(LoginRequestDTO.class))).thenReturn(mockSuccessResponse);

    // When & Then
    mockMvc
      .perform(
        post(AUTH_PREFIX + "/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest))
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void login_WhenEmailIsInvalid_ShouldReturnBadRequest() throws Exception {
    // Given
    LoginRequestDTO loginRequest = LoginRequestDTO.builder()
      .email("invalid-email")
      .password("password@123456")
      .build();

    when(authService.login(any(LoginRequestDTO.class))).thenReturn(mockSuccessResponse);

    // When & Then
    mockMvc
      .perform(
        post(AUTH_PREFIX + "/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest))
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void login_WhenPasswordIsEmpty_ShouldReturnBadRequest() throws Exception {
    LoginRequestDTO loginRequest = LoginRequestDTO.builder()
      .email("test@example.com")
      .password("")
      .build();

    when(authService.login(any(LoginRequestDTO.class))).thenReturn(mockSuccessResponse);

    mockMvc
      .perform(
        post(AUTH_PREFIX + "/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(loginRequest))
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void login_WhenRequestBodyIsEmpty_ShouldReturnBadRequest() throws Exception {
    mockMvc
      .perform(post(AUTH_PREFIX + "/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void register_WhenValidCredentials_ShouldReturnCreatedResponse() throws Exception {
    RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
      .email("email@example.com")
      .password("password@123456")
      .build();

    when(authService.register(any(RegisterRequestDTO.class))).thenReturn(mockSuccessResponse);

    mockMvc
      .perform(
        post(AUTH_PREFIX + "/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(registerRequest))
      )
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.status").value(201))
      .andExpect(jsonPath("$.message").value("Registration successful"))
      .andExpect(jsonPath("$.data.accessToken").value(mockSuccessResponse.getAccessToken()))
      .andExpect(jsonPath("$.data.refreshToken").value(mockSuccessResponse.getRefreshToken()))
      .andExpect(jsonPath("$.data.user.id").value(mockSuccessResponse.getUser().getId()))
      .andExpect(jsonPath("$.data.user.email").value(mockSuccessResponse.getUser().getEmail()))
      .andExpect(
        jsonPath("$.data.user.firstName").value(mockSuccessResponse.getUser().getFirstName())
      )
      .andExpect(
        jsonPath("$.data.user.lastName").value(mockSuccessResponse.getUser().getLastName())
      )
      .andExpect(jsonPath("$.data.user.role").value(mockSuccessResponse.getUser().getRole()));
  }

  @Test
  void register_WhenEmailIsEmpty_ShouldReturnBadRequest() throws Exception {
    RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
      .email("")
      .password("password@123456")
      .build();

    mockMvc
      .perform(
        post(AUTH_PREFIX + "/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(registerRequest))
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void register_WhenEmailIsInvalid_ShouldReturnBadRequest() throws Exception {
    RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
      .email("invalid-email")
      .password("password@123456")
      .build();

    mockMvc
      .perform(
        post(AUTH_PREFIX + "/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(registerRequest))
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void register_WhenPasswordIsLessThan6Characters_ShouldReturnBadRequest() throws Exception {
    RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
      .email("email@example.com")
      .password("12345")
      .build();

    mockMvc
      .perform(
        post(AUTH_PREFIX + "/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(registerRequest))
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void register_WhenPasswordIsEmpty_ShouldReturnBadRequest() throws Exception {
    RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
      .email("email@example.com")
      .password("")
      .build();

    mockMvc
      .perform(
        post(AUTH_PREFIX + "/register")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(registerRequest))
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void register_WhenRequestBodyIsEmpty_ShouldReturnBadRequest() throws Exception {
    mockMvc
      .perform(
        post(AUTH_PREFIX + "/register").contentType(MediaType.APPLICATION_JSON).content("{}")
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void refreshToken_WhenValidRefreshToken_ShouldReturnCreatedResponse() throws Exception {
    RefreshTokenRequestDTO refreshTokenRequest = RefreshTokenRequestDTO.builder()
      .refreshToken("valid-refresh-token")
      .build();

    when(authService.refresh(any(String.class))).thenReturn(mockSuccessResponse);

    mockMvc
      .perform(
        post(AUTH_PREFIX + "/refresh")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(refreshTokenRequest))
      )
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.status").value(201))
      .andExpect(jsonPath("$.message").value("Refresh token successfully"))
      .andExpect(jsonPath("$.data.accessToken").value(mockSuccessResponse.getAccessToken()))
      .andExpect(jsonPath("$.data.refreshToken").value(mockSuccessResponse.getRefreshToken()))
      .andExpect(jsonPath("$.data.user.id").value(mockSuccessResponse.getUser().getId()))
      .andExpect(jsonPath("$.data.user.email").value(mockSuccessResponse.getUser().getEmail()))
      .andExpect(
        jsonPath("$.data.user.firstName").value(mockSuccessResponse.getUser().getFirstName())
      )
      .andExpect(
        jsonPath("$.data.user.lastName").value(mockSuccessResponse.getUser().getLastName())
      )
      .andExpect(jsonPath("$.data.user.role").value(mockSuccessResponse.getUser().getRole()));
  }

  @Test
  void refreshToken_WhenRefreshTokenIsEmpty_ShouldReturnBadRequest() throws Exception {
    RefreshTokenRequestDTO refreshTokenRequest = RefreshTokenRequestDTO.builder()
      .refreshToken("")
      .build();

    mockMvc
      .perform(
        post(AUTH_PREFIX + "/refresh")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(refreshTokenRequest))
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void refreshToken_WhenRefreshTokenIsNull_ShouldReturnBadRequest() throws Exception {
    RefreshTokenRequestDTO refreshTokenRequest = RefreshTokenRequestDTO.builder()
      .refreshToken(null)
      .build();

    mockMvc
      .perform(
        post(AUTH_PREFIX + "/refresh")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(refreshTokenRequest))
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void refreshToken_WhenRequestBodyIsEmpty_ShouldReturnBadRequest() throws Exception {
    mockMvc
      .perform(post(AUTH_PREFIX + "/refresh").contentType(MediaType.APPLICATION_JSON).content("{}"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void logout_WhenUserIsNotLoggedIn_ShouldReturnUnauthorizedResponse() throws Exception {
    mockMvc
      .perform(delete(AUTH_PREFIX + "/logout").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isUnauthorized());
  }

  @Test
  void logout_WhenUserIsLoggedIn_ShouldReturnNoResponse() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();

    mockMvc
      .perform(
        delete(AUTH_PREFIX + "/logout")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isNoContent());
  }

  @Test
  void changePassword_WhenUserIsNotLoggedIn_ShouldReturnUnauthorizedResponse() throws Exception {
    ChangePasswordRequestDTO changePasswordRequest = ChangePasswordRequestDTO.builder()
      .password("oldPassword123")
      .newPassword("newPassword123")
      .build();

    mockMvc
      .perform(
        patch(AUTH_PREFIX + "/password")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(changePasswordRequest))
      )
      .andExpect(status().isUnauthorized());
  }

  @Test
  void changePassword_WhenUserIsLoggedInAndValidRequest_ShouldReturnNoContent() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();

    ChangePasswordRequestDTO changePasswordRequest = ChangePasswordRequestDTO.builder()
      .password("oldPassword123")
      .newPassword("newPassword123")
      .build();

    doNothing()
      .when(authService)
      .changePassword(any(User.class), any(ChangePasswordRequestDTO.class));

    mockMvc
      .perform(
        patch(AUTH_PREFIX + "/password")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "Bearer " + mockAccessToken)
          .content(objectMapper.writeValueAsString(changePasswordRequest))
      )
      .andExpect(status().isNoContent());
  }

  @Test
  void changePassword_WhenNewPasswordIsEmpty_ShouldReturnBadRequest() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();

    ChangePasswordRequestDTO changePasswordRequest = ChangePasswordRequestDTO.builder()
      .password("oldPassword123")
      .build();

    mockMvc
      .perform(
        patch(AUTH_PREFIX + "/password")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "Bearer " + mockAccessToken)
          .content(objectMapper.writeValueAsString(changePasswordRequest))
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void changePassword_WhenNewPasswordIsLessThan6Characters_ShouldReturnBadRequest()
    throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();

    ChangePasswordRequestDTO changePasswordRequest = ChangePasswordRequestDTO.builder()
      .password("oldPassword123")
      .newPassword("12345")
      .build();

    mockMvc
      .perform(
        patch(AUTH_PREFIX + "/password")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "Bearer " + mockAccessToken)
          .content(objectMapper.writeValueAsString(changePasswordRequest))
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void changePassword_WhenRequestBodyIsEmpty_ShouldReturnBadRequest() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();

    mockMvc
      .perform(
        patch(AUTH_PREFIX + "/password")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "Bearer " + mockAccessToken)
          .content("{}")
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }
}
