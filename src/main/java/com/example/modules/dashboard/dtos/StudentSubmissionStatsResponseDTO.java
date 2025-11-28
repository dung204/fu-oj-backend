package com.example.modules.dashboard.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSubmissionStatsResponseDTO {

  @Schema(description = "Ngày thống kê (format: YYYY-MM-DD)", example = "2024-11-25")
  private String date;

  @Schema(
    description = "Tổng số bài tập đã giải được trong ngày (distinct exercises)",
    example = "5"
  )
  private Long totalSolved;

  @Schema(description = "Số bài tập EASY đã giải được trong ngày (score >= 100)", example = "2")
  private Long easy;

  @Schema(description = "Số bài tập MEDIUM đã giải được trong ngày (score >= 200)", example = "2")
  private Long medium;

  @Schema(description = "Số bài tập HARD đã giải được trong ngày (score >= 300)", example = "1")
  private Long hard;
}
