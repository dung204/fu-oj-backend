package com.example.modules.submission_results.dtos;

import com.example.modules.submissions.entities.Submission;
import com.example.modules.test_cases.entities.TestCase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResultResponseDTO {

  @JsonIgnore
  private Submission submission;

  private TestCase testCase;

  private String token;

  private String actualOutput;

  private String stderr;

  private String verdict;

  private String time;

  private String memory;
}
