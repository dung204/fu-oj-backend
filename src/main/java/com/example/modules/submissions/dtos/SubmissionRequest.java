package com.example.modules.submissions.dtos;

import lombok.Data;

@Data
public class SubmissionRequest {

  private String userId;
  private String exerciseId;
  private String sourceCode;
  private String languageCode;
}
