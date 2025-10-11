package com.example.modules.submissions.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunCodeRequest {

  @NotBlank(message = "Exercise ID is required")
  private String exerciseId;

  @NotBlank(message = "Language code is required")
  private String languageCode;

  @NotBlank(message = "Source code is required")
  private String sourceCode;
}
