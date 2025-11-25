package com.example.modules.exams.dtos;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ExamRankingRequestDTO {

  private String groupExamId;
  private String userId;
  private Double totalScore;

  @Min(0)
  private Double minScore;

  private Double maxScore;
}
