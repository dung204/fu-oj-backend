package com.example.modules.auth.services;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.base.BaseServiceIntegrationTest;
import com.example.modules.auth.dtos.AuthTokenDTO;
import com.example.modules.auth.dtos.ChangePasswordRequestDTO;
import com.example.modules.auth.dtos.LoginRequestDTO;
import com.example.modules.auth.dtos.RegisterRequestDTO;
import com.example.modules.auth.entities.Account;
import com.example.modules.auth.exceptions.EmailHasAlreadyBeenUsedException;
import com.example.modules.auth.exceptions.InvalidCredentialsException;
import com.example.modules.auth.exceptions.PasswordNotMatchException;
import com.example.modules.auth.exceptions.TokenInvalidatedException;
import com.example.modules.users.entities.User;
import com.example.modules.users.exceptions.UserNotFoundException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AuthServiceIntegrationTest extends BaseServiceIntegrationTest {

  @Autowired
  private AuthService authService;

  @Autowired
  private JwtService jwtService;

  @Test
  void login_WhenValidCredentials_ShouldReturnAuthTokenDTO() {
    LoginRequestDTO loginRequest = LoginRequestDTO.builder()
      .email("email@example.com")
      .password("password@123456")
      .build();

    AuthTokenDTO tokenDTO = authService.login(loginRequest);

    assertNotNull(tokenDTO.getAccessToken());
    assertNotNull(tokenDTO.getRefreshToken());
    assertEquals("email@example.com", tokenDTO.getUser().getEmail());
  }

  @Test
  void login_WhenInvalidEmail_ShouldThrowInvalidCredentialsException() {
    LoginRequestDTO loginRequest = LoginRequestDTO.builder()
      .email("invalid@example.com")
      .password("password@123456")
      .build();

    assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
  }

  @Test
  void login_WhenInvalidPassword_ShouldThrowInvalidCredentialsException() {
    LoginRequestDTO loginRequest = LoginRequestDTO.builder()
      .email("email@example.com")
      .password("invalidpassword")
      .build();

    assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
  }

  @Test
  void register_WhenValidRequest_ShouldReturnAuthTokenDTO() {
    RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
      .email("newemail@example.com")
      .password("newpassword")
      .build();

    AuthTokenDTO tokenDTO = authService.register(registerRequest);

    assertNotNull(tokenDTO.getAccessToken());
    assertNotNull(tokenDTO.getRefreshToken());
    assertEquals("newemail@example.com", tokenDTO.getUser().getEmail());
  }

  @Test
  void register_WhenEmailAlreadyUsedAndExistingAccountIsEnabled_ShouldThrowEmailHasAlreadyBeenUsedException() {
    RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
      .email("email@example.com")
      .password("newpassword")
      .build();

    assertThrows(EmailHasAlreadyBeenUsedException.class, () ->
      authService.register(registerRequest)
    );
  }

  @Test
  void register_WhenEmailAlreadyUsedAndExistingAccountNotEnabled_ShouldCreateUserAndReturnAuthTokenDTO() {
    Account account = getAccount();
    account.setDeletedTimestamp(Instant.now());
    accountsRepository.save(account);

    RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
      .email("email@example.com")
      .password("newpassword")
      .build();

    AuthTokenDTO tokenDTO = authService.register(registerRequest);

    assertNotNull(tokenDTO.getAccessToken());
    assertNotNull(tokenDTO.getRefreshToken());
    assertEquals("email@example.com", tokenDTO.getUser().getEmail());
  }

  @Test
  void refresh_WhenTokenIsValidAndUserExists_ShouldReturnAuthTokenDTO() {
    User user = getUser();
    String refreshToken = jwtService.generateRefreshToken(user);

    AuthTokenDTO tokenDTO = authService.refresh(refreshToken);

    assertNotNull(tokenDTO.getAccessToken());
    assertNotNull(tokenDTO.getRefreshToken());
    assertEquals(user.getAccount().getEmail(), tokenDTO.getUser().getEmail());
  }

  @Test
  void refresh_WhenTokenIsInvalidated_ShouldThrowTokenInvalidatedException() throws Exception {
    User user = getUser();
    String refreshToken = jwtService.generateRefreshToken(user);

    Thread.sleep(2000);
    authService.logout(user);

    assertThrows(TokenInvalidatedException.class, () -> authService.refresh(refreshToken));
  }

  @Test
  void refresh_WhenUserNotFound_ShouldThrowUserNotFoundException() {
    User user = getUser();
    String refreshToken = jwtService.generateRefreshToken(user);

    usersRepository.deleteAll();

    assertThrows(UserNotFoundException.class, () -> authService.refresh(refreshToken));
  }

  @Test
  void changePassword_WhenCurrentPasswordIsNull_ShouldUpdatePasswordAndInvalidateTokens() {
    User user = getUser();
    Account account = user.getAccount();
    account.setPassword(null);
    accountsRepository.save(account);

    ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
      .password(null)
      .newPassword("newPassword")
      .build();

    authService.changePassword(user, request);
    Account savedAccount = getAccount();

    assertTrue(passwordEncoder.matches(request.getNewPassword(), savedAccount.getPassword()));
  }

  @Test
  void changePassword_WhenRequestPasswordIsNullAndCurrentPasswordIsNotNull_ShouldThrowResponseStatusException() {
    User user = getUser();

    ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
      .password(null)
      .newPassword("newPassword")
      .build();

    ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
      authService.changePassword(user, request)
    );

    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }

  @Test
  void changePassword_WhenPasswordDoesNotMatchCurrent_ShouldThrowPasswordNotMatchException() {
    User user = getUser();

    ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
      .password("wrongPassword")
      .newPassword("newPassword")
      .build();

    assertThrows(PasswordNotMatchException.class, () -> authService.changePassword(user, request));
  }

  @Test
  void changePassword_WhenPasswordMatchesCurrent_ShouldUpdatePasswordAndInvalidateTokens() {
    User user = getUser();

    ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
      .password("password@123456")
      .newPassword("newPassword")
      .build();

    authService.changePassword(user, request);
    Account savedAccount = getAccount();

    assertTrue(passwordEncoder.matches(request.getNewPassword(), savedAccount.getPassword()));
  }
}
