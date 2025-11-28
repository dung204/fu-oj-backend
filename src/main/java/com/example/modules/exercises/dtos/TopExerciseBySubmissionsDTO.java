package com.example.modules.exercises.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopExerciseBySubmissionsDTO {

  @Schema(description = "ID của bài tập", example = "550e8400-e29b-41d4-a716-446655440000")
  private String id;

  @Schema(description = "Code của bài tập", example = "EX001")
  private String code;

  @Schema(description = "Tiêu đề bài tập", example = "Tính tổng hai số")
  private String title;

  @Schema(description = "Độ khó của bài tập", example = "EASY")
  private String difficulty;

  @Schema(description = "Số lượt nộp của bài tập", example = "150")
  private Long submissionCount;
}
