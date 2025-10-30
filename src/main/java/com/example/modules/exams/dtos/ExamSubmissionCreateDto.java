package com.example.modules.exams.dtos;

import com.example.modules.submissions.dtos.SubmissionRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ExamSubmissionCreateDto extends SubmissionRequest {

  @NotBlank(message = "ExamId is required")
  private String examId;

  @NotBlank(message = "User id is required")
  private String userId;

  @NotBlank(message = "ExerciseId is required")
  private String exerciseId;
}
