package com.example.modules.submissions.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseResultDTO {

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

  private Judge0StatusDTO status;

  private Boolean passed;

  private Boolean isPublic;
}
