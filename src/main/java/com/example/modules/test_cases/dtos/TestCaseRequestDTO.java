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

  @NotBlank(message = "Exercise ID is required")
  private String exerciseId;

  private String input;

  private String output;

  @NotNull(message = "IsPublic is required")
  private Boolean isPublic;
}
