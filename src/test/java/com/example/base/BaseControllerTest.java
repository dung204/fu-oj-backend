package com.example.base;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.base.configs.ObjectMapperConfig;
import com.example.base.configs.SecurityConfig;
import com.example.modules.auth.entities.Account;
import com.example.modules.auth.enums.Role;
import com.example.modules.auth.repositories.AccountsRepository;
import com.example.modules.auth.services.AuthService;
import com.example.modules.auth.services.JwtService;
import com.example.modules.minio.dtos.MinioFileResponse;
import com.example.modules.users.dtos.UserProfileDTO;
import com.example.modules.users.entities.User;
import com.example.modules.users.repositories.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Import({ SecurityConfig.class, ObjectMapperConfig.class })
@Tag("unit")
public abstract class BaseControllerTest {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected ObjectMapper objectMapper;

  @MockitoBean
  protected JwtService jwtService;

  @MockitoBean
  protected AuthService authService;

  @MockitoBean
  protected AccountsRepository accountsRepository;

  @MockitoBean
  protected UsersRepository usersRepository;

  protected MockUserLoginPayload mockUserLogin() {
    String userId = "user-123";
    String email = "email@example.com";
    Instant currentTimestamp = Instant.now();
    User mockUser = User.builder()
      .id(userId)
      .account(Account.builder().email(email).password("password@123456").build())
      .firstName("John")
      .lastName("Doe")
      .createdTimestamp(currentTimestamp)
      .updatedTimestamp(currentTimestamp)
      .build();
    String accessToken = "mock-access-token";

    Jws<Claims> mockJws = mock(Jws.class);
    Claims mockClaims = mock(Claims.class);
    mockClaims.put("role", Role.USER.getValue());

    when(mockJws.getPayload()).thenReturn(mockClaims);
    when(mockJws.getPayload().get("role", String.class)).thenReturn(Role.USER.getValue());
    when(mockClaims.getSubject()).thenReturn(userId);

    when(jwtService.verifyAccessToken(accessToken)).thenReturn(mockJws);

    when(jwtService.isTokenInvalidated(eq(userId), any(Date.class))).thenReturn(false);

    when(usersRepository.findById(userId)).thenReturn(Optional.of(mockUser));

    when(usersRepository.findByAccountEmail(email)).thenReturn(Optional.of(mockUser));

    doNothing().when(authService).logout(mockUser);

    return new MockUserLoginPayload(accessToken, mockUser);
  }

  protected UserProfileDTO createMockUserProfile(User user) {
    return UserProfileDTO.builder()
      .id(user.getId())
      .email(user.getAccount().getEmail())
      .firstName(user.getFirstName())
      .lastName(user.getLastName())
      .avatar(
        user.getAvatar() == null
          ? null
          : MinioFileResponse.builder().fileName(user.getAvatar()).url("image url").build()
      )
      .role(user.getAccount().getRole().getValue())
      .createdTimestamp(user.getCreatedTimestamp().toString())
      .updatedTimestamp(user.getUpdatedTimestamp().toString())
      .build();
  }

  protected static record MockUserLoginPayload(String accessToken, User user) {}
}
