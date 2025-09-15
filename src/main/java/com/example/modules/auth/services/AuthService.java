package com.example.modules.auth.services;

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
import com.example.modules.users.entities.User;
import com.example.modules.users.exceptions.UserNotFoundException;
import com.example.modules.users.repositories.UsersRepository;
import com.example.modules.users.utils.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AccountsRepository accountsRepository;
  private final UsersRepository usersRepository;
  private final JwtService jwtService;
  private final RedisService redisService;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  public AuthTokenDTO login(LoginRequestDTO loginRequest) {
    String email = loginRequest.getEmail();
    String password = loginRequest.getPassword();

    Account account = accountsRepository
      .findByEmail(email)
      .orElseThrow(() -> new InvalidCredentialsException());

    if (!passwordEncoder.matches(password, account.getPassword())) {
      throw new InvalidCredentialsException();
    }

    User user = usersRepository.findByAccount(account).get();
    return getTokenResponse(user);
  }

  public AuthTokenDTO register(RegisterRequestDTO registerRequest) {
    final String email = registerRequest.getEmail();
    final String password = registerRequest.getPassword();

    final Optional<Account> existingAccount = accountsRepository.findByEmail(email);
    final User savedUser;

    if (!existingAccount.isPresent()) {
      final Account savedAccount = accountsRepository.save(
        Account.builder().email(email).password(passwordEncoder.encode(password)).build()
      );

      savedUser = usersRepository.save(User.builder().account(savedAccount).build());
    } else if (existingAccount.get().isEnabled()) {
      throw new EmailHasAlreadyBeenUsedException();
    } else {
      final Account account = existingAccount.get();
      account.setEmail(email);
      account.setPassword(passwordEncoder.encode(password));
      account.setDeletedTimestamp(null);
      final Account savedAccount = accountsRepository.save(account);
      final User user = usersRepository
        .findByAccount(account)
        .orElse(User.builder().account(savedAccount).build());
      savedUser = usersRepository.save(user);
    }

    return getTokenResponse(savedUser);
  }

  public AuthTokenDTO refresh(String refreshToken) {
    final Jws<Claims> decodedRefreshToken = jwtService.verifyRefreshToken(refreshToken);
    final String userId = decodedRefreshToken.getPayload().getSubject();
    final Date tokenIssuedAt = decodedRefreshToken.getPayload().getIssuedAt();

    if (jwtService.isTokenInvalidated(userId, tokenIssuedAt)) {
      throw new TokenInvalidatedException();
    }

    final User user = usersRepository
      .findById(userId)
      .orElseThrow(() -> new UserNotFoundException());

    invalidateTokens(userId);
    return getTokenResponse(user);
  }

  public void logout(User user) {
    invalidateTokens(user.getId());
  }

  public void changePassword(User user, ChangePasswordRequestDTO request) {
    String currentPassword = user.getAccount().getPassword();

    if (currentPassword != null && request.getPassword() == null) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Field `password` is required for this account."
      );
    }

    if (
      currentPassword != null && !passwordEncoder.matches(request.getPassword(), currentPassword)
    ) {
      throw new PasswordNotMatchException();
    }

    Account userAccount = user.getAccount();
    userAccount.setPassword(passwordEncoder.encode(request.getNewPassword()));
    accountsRepository.save(userAccount);

    invalidateTokens(user.getId());
  }

  private AuthTokenDTO getTokenResponse(User user) {
    final String accessToken = jwtService.generateAccessToken(user);
    final String refreshToken = jwtService.generateRefreshToken(user);

    return AuthTokenDTO.builder()
      .accessToken(accessToken)
      .refreshToken(refreshToken)
      .user(userMapper.toUserProfileDTO(user))
      .build();
  }

  private void invalidateTokens(String userId) {
    redisService.set(
      "user:%s:tokens:invalidated_before".formatted(userId),
      Instant.now().truncatedTo(ChronoUnit.SECONDS)
    );
  }
}
