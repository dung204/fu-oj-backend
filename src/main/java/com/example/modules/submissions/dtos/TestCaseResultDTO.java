package com.example.modules.submissions.dtos;

import com.example.modules.submissions.enums.Verdict;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseResultDTO {

  private String submissionId;

  private String token;

  private String userId;

  private String testCaseId;

  private Integer testCaseIndex;

  private String input;

  private String expectedOutput;

  private String actualOutput;

  private String stderr;

  private String compileOutput;

  private String time;

  private Integer memory;

  private Verdict verdict;

  private Boolean passed;

  private Boolean isPublic;
}
