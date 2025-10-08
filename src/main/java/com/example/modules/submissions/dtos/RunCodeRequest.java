package com.example.modules.submissions.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunCodeRequest {

  private String sourceCode;
  private String languageCode;
  private String input; // input để test
  private String expectedOutput; // output mong đợi (optional)
}
