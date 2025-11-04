package com.example.modules.exams.dtos;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultResponseDto {

  private String examId;
  private String examCode;
  private String examTitle;
  private Instant startTime;
  private Instant endTime;
  private String userId;
  private String userName;
  private List<ExamSubmissionDetail> submissions;
  private Double totalScore;
  private Integer totalExercises;
  private Integer completedExercises;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ExamSubmissionDetail {

    private String exerciseId;
    private String exerciseTitle;
    private String exerciseCode;
    private String submissionId;
    private Double score;
    private Boolean isAccepted;
    private Integer passedTestCases;
    private Integer totalTestCases;
    private Instant submittedAt;
  }
}
