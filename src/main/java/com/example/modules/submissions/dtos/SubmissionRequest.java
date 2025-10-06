package com.example.modules.submissions.dtos;

import lombok.Data;

@Data
public class SubmissionRequest {

  private Long userId;
  private Long exerciseId;
  private String sourceCode;
  private String languageCode;
}
