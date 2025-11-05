package com.example.modules.auth.controllers;

import static com.example.base.utils.AppRoutes.AUTH_PREFIX;

import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.annotations.Public;
import com.example.modules.auth.dtos.AuthTokenDTO;
import com.example.modules.auth.dtos.ChangePasswordRequestDTO;
import com.example.modules.auth.dtos.LoginRequestDTO;
import com.example.modules.auth.dtos.RefreshTokenRequestDTO;
import com.example.modules.auth.dtos.RegisterRequestDTO;
import com.example.modules.auth.entities.Account;
import com.example.modules.auth.repositories.AccountsRepository;
import com.example.modules.auth.services.AuthService;
import com.example.modules.email.service.EmailService;
import com.example.modules.file.excel.utils.PasswordUtils;
import com.example.modules.users.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = AUTH_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "auth", description = "Operations related to authentication & authorization")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final EmailService emailService;
  private final AccountsRepository accountsRepository;

  @Public
  @Operation(
    summary = "Login",
    responses = {
      @ApiResponse(responseCode = "201", description = "Login successfully"),
      @ApiResponse(
        responseCode = "400",
        description = """
        - Email is empty or invalid
        - Password is empty
        """,
        content = @Content
      ),
      @ApiResponse(
        responseCode = "401",
        description = "Email or password is incorrect",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true)
  @PostMapping(path = "/login")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<AuthTokenDTO> login(@RequestBody @Valid LoginRequestDTO loginRequest) {
    return SuccessResponseDTO.<AuthTokenDTO>builder()
      .status(201)
      .message("Login successfully")
      .data(authService.login(loginRequest))
      .build();
  }

  @Public
  @Operation(
    summary = "Register",
    responses = {
      @ApiResponse(responseCode = "201", description = "Register successfully"),
      @ApiResponse(
        responseCode = "400",
        description = """
        - Email is empty or invalid
        - Password does not have at least 6 characters
        """,
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true)
  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<AuthTokenDTO> register(
    @RequestBody @Valid RegisterRequestDTO registerRequest
  ) {
    return SuccessResponseDTO.<AuthTokenDTO>builder()
      .status(201)
      .message("Registration successful")
      .data(authService.register(registerRequest))
      .build();
  }

  @Public
  @Operation(
    summary = "Create new (refresh) tokens",
    responses = {
      @ApiResponse(responseCode = "201", description = "Refresh token successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "JWT error (malformed, expired, ...)",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "401",
        description = "Refresh token is blacklisted",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true)
  @PostMapping("/refresh")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<AuthTokenDTO> refreshToken(
    @RequestBody @Valid RefreshTokenRequestDTO refreshTokenRequestDTO
  ) {
    return SuccessResponseDTO.<AuthTokenDTO>builder()
      .status(201)
      .message("Refresh token successfully")
      .data(authService.refresh(refreshTokenRequestDTO.getRefreshToken()))
      .build();
  }

  @Operation(
    summary = "Logout",
    responses = {
      @ApiResponse(
        responseCode = "204",
        description = "Logged out successfully",
        content = @Content
      ),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @DeleteMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@CurrentUser User currentUser) {
    authService.logout(currentUser);
  }

  @Operation(
    summary = "Change password of the current user",
    responses = {
      @ApiResponse(
        responseCode = "204",
        description = "Password changed successfully",
        content = @Content
      ),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PatchMapping("/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void changePassword(
    @CurrentUser User currentUser,
    @RequestBody @Valid ChangePasswordRequestDTO request
  ) {
    authService.changePassword(currentUser, request);
  }

  @Public
  @Operation(
    summary = "Send a password reset email",
    description = """
    Sends an email containing a randomly generated password or reset code
    to the user's email address.
    The email content is rendered using a predefined HTML template (`takepassword-email`).
    """,
    parameters = {
      @Parameter(
        name = "to",
        description = "Email address of the recipient",
        required = true,
        example = "user@example.com"
      ),
    },
    responses = {
      @ApiResponse(
        responseCode = "204",
        description = "Email sent successfully. No content is returned."
      ),
      @ApiResponse(
        responseCode = "400",
        description = """
        - Missing or invalid email address
        - Email template not found
        """,
        content = @Content
      ),
      @ApiResponse(
        responseCode = "500",
        description = "Internal Server Error while sending email",
        content = @Content
      ),
    }
  )
  @GetMapping("/forget-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void forgetPassword(@RequestParam String to) throws MessagingException {
    emailService.sendEmailWithTemplate(
      to,
      "SEND PASSWORD ",
      "takepassword-email",
      Map.of("name", to, "code", PasswordUtils.generateRandomPassword(8))
    );
  }

  @Public
  @GetMapping("/active-account/{email}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void activeAccount(@PathVariable String email) {
    Account account = accountsRepository.findAccountByEmail(email);
    account.setDeletedTimestamp(null);
    accountsRepository.save(account);
  }
}
