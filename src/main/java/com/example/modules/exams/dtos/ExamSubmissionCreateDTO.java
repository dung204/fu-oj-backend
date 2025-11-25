package com.example.modules.exams.dtos;

import com.example.modules.submissions.dtos.SubmissionRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ExamSubmissionCreateDTO extends SubmissionRequest {

  @NotBlank(message = "GroupExamId is required")
  private String groupExamId;

  @NotBlank(message = "ExerciseId is required")
  private String exerciseId;
}
