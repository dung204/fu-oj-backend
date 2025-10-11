package com.example.modules.Judge0.dtos;

import java.util.List;
import lombok.Data;

@Data
public class SubmissionRequestDTO {

  private String sourceCode;
  private String languageId;
  private List<String> testInputs;
}
