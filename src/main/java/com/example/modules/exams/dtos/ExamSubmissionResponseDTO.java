package com.example.modules.exams.dtos;

import com.example.base.dtos.EntityDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * DTO response cơ bản cho ExamSubmission
 */
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmissionResponseDTO extends EntityDTO {

  private String examId;
  private String userId;
  private String exerciseId;
  private String submissionId;
  private Double score;
  private Integer passedTestCases;
  private Integer totalTestCases;
  private Boolean isAccepted;
  private String time;
  private String memory;
  private Double timeLimit;
}
