package com.example.modules.auth.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.base.BaseServiceTest;
import com.example.modules.auth.dtos.AuthTokenDTO;
import com.example.modules.auth.dtos.ChangePasswordRequestDTO;
import com.example.modules.auth.dtos.LoginRequestDTO;
import com.example.modules.auth.dtos.RegisterRequestDTO;
import com.example.modules.auth.entities.Account;
import com.example.modules.auth.exceptions.EmailHasAlreadyBeenUsedException;
import com.example.modules.auth.exceptions.InvalidCredentialsException;
import com.example.modules.auth.exceptions.PasswordNotMatchException;
import com.example.modules.auth.exceptions.TokenInvalidatedException;
import com.example.modules.auth.repositories.AccountsRepository;
import com.example.modules.redis.services.RedisService;
import com.example.modules.users.dtos.UserProfileDTO;
import com.example.modules.users.entities.User;
import com.example.modules.users.exceptions.UserNotFoundException;
import com.example.modules.users.repositories.UsersRepository;
import com.example.modules.users.utils.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

public class AuthServiceTest extends BaseServiceTest {

  @Mock
  private AccountsRepository accountsRepository;

  @Mock
  private UsersRepository usersRepository;

  @Mock
  private JwtService jwtService;

  @Mock
  private RedisService redisService;

  @Mock
  private UserMapper userMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private AuthService authService;

  @Test
  void login_WhenCredentialsAreValid_ShouldReturnAuthTokenDTO() {
    String email = "test@example.com";
    String password = "password";
    LoginRequestDTO loginRequest = LoginRequestDTO.builder()
      .email(email)
      .password(password)
      .build();
    Account mockAccount = getMockAccount();
    User mockUser = getMockUser();
    UserProfileDTO userProfileDTO = getMockUserProfile();
    AuthTokenDTO tokenDTO = AuthTokenDTO.builder()
      .accessToken("access token")
      .refreshToken("refresh token")
      .user(userProfileDTO)
      .build();

    when(accountsRepository.findByEmail(email)).thenReturn(Optional.of(mockAccount));
    when(passwordEncoder.matches(password, mockUser.getAccount().getPassword())).thenReturn(true);
    when(usersRepository.findByAccount(mockAccount)).thenReturn(Optional.of(mockUser));
    when(jwtService.generateAccessToken(mockUser)).thenReturn(tokenDTO.getAccessToken());
    when(jwtService.generateRefreshToken(mockUser)).thenReturn(tokenDTO.getRefreshToken());
    when(userMapper.toUserProfileDTO(mockUser)).thenReturn(userProfileDTO);

    AuthTokenDTO result = authService.login(loginRequest);

    assertNotNull(result);
    assertEquals(tokenDTO.getAccessToken(), result.getAccessToken());
    assertEquals(tokenDTO.getRefreshToken(), result.getRefreshToken());
    assertEquals(tokenDTO.getUser(), result.getUser());
  }

  @Test
  void login_WhenAccountNotFound_ShouldThrowInvalidCredentialsException() {
    String email = "notfound@example.com";
    String password = "password";

    LoginRequestDTO loginRequest = LoginRequestDTO.builder()
      .email(email)
      .password(password)
      .build();

    when(accountsRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
  }

  @Test
  void login_WhenPasswordDoesNotMatch_ShouldThrowInvalidCredentialsException() {
    String email = "test@example.com";
    String password = "wrongPassword";

    Account mockAccount = getMockAccount();

    LoginRequestDTO loginRequest = LoginRequestDTO.builder()
      .email(email)
      .password(password)
      .build();

    when(accountsRepository.findByEmail(email)).thenReturn(Optional.of(mockAccount));
    when(passwordEncoder.matches(password, mockAccount.getPassword())).thenReturn(false);

    assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
  }

  @Test
  void register_WhenEmailNotAlreadyUsedAndNoExistingAccount_ShouldReturnAuthTokenDTO() {
    String email = "newuser@example.com";
    String password = "newpassword";
    RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
      .email(email)
      .password(password)
      .build();

    Account savedAccount = Account.builder().email(email).password("encodedPassword").build();
    User savedUser = User.builder().account(savedAccount).build();
    UserProfileDTO userProfileDTO = getMockUserProfile();
    AuthTokenDTO tokenDTO = AuthTokenDTO.builder()
      .accessToken("access token")
      .refreshToken("refresh token")
      .user(userProfileDTO)
      .build();

    when(accountsRepository.findByEmail(email)).thenReturn(Optional.empty());
    when(passwordEncoder.encode(password)).thenReturn(savedAccount.getPassword());
    when(accountsRepository.save(any(Account.class))).thenReturn(savedAccount);
    when(usersRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtService.generateAccessToken(savedUser)).thenReturn(tokenDTO.getAccessToken());
    when(jwtService.generateRefreshToken(savedUser)).thenReturn(tokenDTO.getRefreshToken());
    when(userMapper.toUserProfileDTO(savedUser)).thenReturn(userProfileDTO);

    AuthTokenDTO result = authService.register(registerRequest);

    assertNotNull(result);
    assertEquals(tokenDTO.getAccessToken(), result.getAccessToken());
    assertEquals(tokenDTO.getRefreshToken(), result.getRefreshToken());
    assertEquals(tokenDTO.getUser(), result.getUser());
  }

  @Test
  void register_WhenEmailAlreadyUsedAndExistingAccountIsEnabled_ShouldThrowEmailHasAlreadyBeenUsedException() {
    String email = "existing@example.com";
    String password = "password";
    RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
      .email(email)
      .password(password)
      .build();

    Account existingAccount = Account.builder().email(email).password("encodedPassword").build();

    when(accountsRepository.findByEmail(email)).thenReturn(Optional.of(existingAccount));

    assertThrows(EmailHasAlreadyBeenUsedException.class, () ->
      authService.register(registerRequest)
    );
  }

  @Test
  void register_WhenEmailAlreadyUsedAndExistingAccountNotEnabled_ShouldCreateUserAndReturnAuthTokenDTO() {
    String email = "existing@example.com";
    String password = "password";
    RegisterRequestDTO registerRequest = RegisterRequestDTO.builder()
      .email(email)
      .password(password)
      .build();

    Account existingAccount = Account.builder()
      .email(email)
      .password("encodedPassword")
      .deletedTimestamp(Instant.now())
      .build();
    Account savedAccount = Account.builder().email(email).password("encodedPassword").build();
    User savedUser = User.builder().account(savedAccount).build();
    UserProfileDTO userProfileDTO = getMockUserProfile();
    AuthTokenDTO tokenDTO = AuthTokenDTO.builder()
      .accessToken("access token")
      .refreshToken("refresh token")
      .user(userProfileDTO)
      .build();

    when(accountsRepository.findByEmail(email)).thenReturn(Optional.of(existingAccount));
    when(usersRepository.save(any(User.class))).thenReturn(savedUser);
    when(jwtService.generateAccessToken(savedUser)).thenReturn(tokenDTO.getAccessToken());
    when(jwtService.generateRefreshToken(savedUser)).thenReturn(tokenDTO.getRefreshToken());
    when(userMapper.toUserProfileDTO(savedUser)).thenReturn(userProfileDTO);

    AuthTokenDTO result = authService.register(registerRequest);

    assertNotNull(result);
    assertEquals(tokenDTO.getAccessToken(), result.getAccessToken());
    assertEquals(tokenDTO.getRefreshToken(), result.getRefreshToken());
    assertEquals(tokenDTO.getUser(), result.getUser());
  }

  @Test
  void refresh_WhenTokenIsValidAndUserExists_ShouldReturnAuthTokenDTO() {
    String refreshToken = "validRefreshToken";
    String userId = "user123";
    Date issuedAt = new Date();

    Jws<Claims> mockJws = mock(Jws.class);
    Claims mockClaims = mock(Claims.class);
    User mockUser = getMockUser();
    AuthTokenDTO tokenDTO = AuthTokenDTO.builder()
      .accessToken("access token")
      .refreshToken("refresh token")
      .user(getMockUserProfile())
      .build();

    when(jwtService.verifyRefreshToken(refreshToken)).thenReturn(mockJws);
    when(mockJws.getPayload()).thenReturn(mockClaims);
    when(mockClaims.getSubject()).thenReturn(userId);
    when(mockClaims.getIssuedAt()).thenReturn(issuedAt);
    when(jwtService.isTokenInvalidated(userId, issuedAt)).thenReturn(false);
    when(usersRepository.findById(userId)).thenReturn(Optional.of(mockUser));
    when(jwtService.generateAccessToken(mockUser)).thenReturn(tokenDTO.getAccessToken());
    when(jwtService.generateRefreshToken(mockUser)).thenReturn(tokenDTO.getRefreshToken());
    when(userMapper.toUserProfileDTO(mockUser)).thenReturn(tokenDTO.getUser());

    AuthTokenDTO result = authService.refresh(refreshToken);

    assertNotNull(result);
    assertEquals(tokenDTO.getAccessToken(), result.getAccessToken());
    assertEquals(tokenDTO.getRefreshToken(), result.getRefreshToken());
    assertEquals(tokenDTO.getUser(), result.getUser());
  }

  @Test
  void refresh_WhenTokenIsInvalidated_ShouldThrowTokenInvalidatedException() {
    String refreshToken = "invalidatedRefreshToken";
    String userId = "user123";
    Date issuedAt = new Date();

    Jws<Claims> mockJws = mock(Jws.class);
    Claims mockClaims = mock(Claims.class);

    when(jwtService.verifyRefreshToken(refreshToken)).thenReturn(mockJws);
    when(mockJws.getPayload()).thenReturn(mockClaims);
    when(mockClaims.getSubject()).thenReturn(userId);
    when(mockClaims.getIssuedAt()).thenReturn(issuedAt);
    when(jwtService.isTokenInvalidated(userId, issuedAt)).thenReturn(true);

    assertThrows(TokenInvalidatedException.class, () -> authService.refresh(refreshToken));
  }

  @Test
  void refresh_WhenUserNotFound_ShouldThrowUserNotFoundException() {
    String refreshToken = "validRefreshToken";
    String userId = "user123";
    Date issuedAt = new Date();

    Jws<Claims> mockJws = mock(Jws.class);
    Claims mockClaims = mock(Claims.class);

    when(jwtService.verifyRefreshToken(refreshToken)).thenReturn(mockJws);
    when(mockJws.getPayload()).thenReturn(mockClaims);
    when(mockClaims.getSubject()).thenReturn(userId);
    when(mockClaims.getIssuedAt()).thenReturn(issuedAt);
    when(jwtService.isTokenInvalidated(userId, issuedAt)).thenReturn(false);
    when(usersRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> authService.refresh(refreshToken));
  }

  @Test
  void changePassword_WhenCurrentPasswordIsNull_ShouldUpdatePasswordAndInvalidateTokens() {
    User mockUser = getMockUser();
    Account mockAccount = mockUser.getAccount();
    mockAccount.setPassword(null);

    ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
      .password(null)
      .newPassword("newPassword")
      .build();

    when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedNewPassword");
    when(accountsRepository.save(mockAccount)).thenReturn(mockAccount);

    authService.changePassword(mockUser, request);

    assertEquals("encodedNewPassword", mockAccount.getPassword());
  }

  @Test
  void changePassword_WhenRequestPasswordIsNullAndCurrentPasswordIsNotNull_ShouldThrowResponseStatusException() {
    User mockUser = getMockUser();
    Account mockAccount = mockUser.getAccount();
    mockAccount.setPassword("currentPassword");

    ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
      .password(null)
      .newPassword("newPassword")
      .build();

    ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
      authService.changePassword(mockUser, request)
    );
    assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
  }

  @Test
  void changePassword_WhenPasswordDoesNotMatchCurrent_ShouldThrowPasswordNotMatchException() {
    User mockUser = getMockUser();
    Account mockAccount = mockUser.getAccount();
    mockAccount.setPassword("currentPassword");

    ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
      .password("wrongPassword")
      .newPassword("newPassword")
      .build();

    when(passwordEncoder.matches("wrongPassword", "currentPassword")).thenReturn(false);

    assertThrows(PasswordNotMatchException.class, () ->
      authService.changePassword(mockUser, request)
    );
  }

  @Test
  void changePassword_WhenPasswordMatchesCurrent_ShouldUpdatePasswordAndInvalidateTokens() {
    User mockUser = getMockUser();
    Account mockAccount = mockUser.getAccount();
    mockAccount.setPassword("currentPassword");

    ChangePasswordRequestDTO request = ChangePasswordRequestDTO.builder()
      .password("currentPassword")
      .newPassword("newPassword")
      .build();

    when(passwordEncoder.matches("currentPassword", "currentPassword")).thenReturn(true);
    when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
    when(accountsRepository.save(mockAccount)).thenReturn(mockAccount);

    authService.changePassword(mockUser, request);

    assertEquals("encodedNewPassword", mockAccount.getPassword());
  }
}
