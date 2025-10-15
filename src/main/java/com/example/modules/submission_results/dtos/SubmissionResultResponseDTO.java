package com.example.modules.submission_results.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResultResponseDTO {

  private String token;

  private String actualOutput;

  private String stderr;

  private String verdict;

  private String time;

  private String memory;
}
