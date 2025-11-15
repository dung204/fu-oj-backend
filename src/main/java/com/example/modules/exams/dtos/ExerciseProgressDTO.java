package com.example.modules.exams.dtos;

import com.example.base.utils.SwaggerExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseProgressDTO {

  @Schema(description = "Exercise ID", example = SwaggerExamples.UUID)
  private String exerciseId;

  @Schema(description = "Exercise code", example = SwaggerExamples.EXERCISE_CODE)
  private String exerciseCode;

  @Schema(description = "Exercise title")
  private String exerciseTitle;

  @Schema(description = "Order index of the exercise in the exam")
  private Integer order;

  @Schema(description = "Score for this exercise (from ExamSubmission)", example = "8.5")
  private Double score;

  @Schema(description = "Whether the student has submitted this exercise")
  private Boolean hasSubmitted;
}
