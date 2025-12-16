package com.example.modules.auth.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOTPRequestDTO {

  @NotBlank
  @Email
  private String email;

  @NotBlank
  private String otp;
}
