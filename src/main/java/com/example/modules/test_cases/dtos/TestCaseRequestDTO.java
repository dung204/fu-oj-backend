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

  @NotBlank
  private String input;

  @NotBlank
  private String output;

  @NotNull
  private Boolean isPublic;
}
