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
import com.example.modules.auth.services.AuthService;
import com.example.modules.users.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = AUTH_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "auth", description = "Operations related to authentication & authorization")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

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
}
