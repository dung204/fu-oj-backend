package com.example.modules.submissions.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResultUpdateEventDTO {

  String submissionId;
  String sourceCode;
  String languageCode;
  List<String> testInputs;
  List<String> expectedOutputs;
}
