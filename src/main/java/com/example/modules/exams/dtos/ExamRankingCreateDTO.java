package com.example.modules.exams.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamRankingCreateDTO {

  @NotBlank(message = "Exam ID is required")
  @Schema(description = "ID của exam", example = "550e8400-e29b-41d4-a716-446655440000")
  private String examId;

  @NotBlank(message = "User ID is required")
  @Schema(description = "ID của user (student)", example = "550e8400-e29b-41d4-a716-446655440001")
  private String userId;

  @NotNull(message = "Number Of Exercises is required")
  @Schema(description = "Quality of excerise", example = "3")
  private Double numberOfExercises;

  private boolean completed = false;
}
