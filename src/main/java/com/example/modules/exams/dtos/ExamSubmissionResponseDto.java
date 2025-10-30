package com.example.modules.exams.dtos;

import com.example.base.dtos.EntityDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmissionResponseDto extends EntityDTO {

  private String examId;
  private String userId;
  private String exerciseId;
  private String submissionId;
  private Double score;
}
