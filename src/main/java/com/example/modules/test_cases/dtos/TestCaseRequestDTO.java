package com.example.modules.test_cases.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseRequestDTO {

  // Optional: nếu có id thì update, không có id thì tạo mới
  private String id;

  @NotBlank
  private String input;

  @NotBlank
  private String output;

  @NotNull
  private Boolean isPublic;
}
