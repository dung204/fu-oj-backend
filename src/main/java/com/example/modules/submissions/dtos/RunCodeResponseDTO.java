package com.example.modules.submissions.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunCodeResponseDTO {

  private String exerciseId;

  private String exerciseTitle;

  private Integer totalTestCases;

  private Integer passedTestCases;

  private Boolean allPassed;

  private List<TestCaseResultDTO> results;
}
