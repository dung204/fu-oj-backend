package com.example.modules.dashboard.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSubmissionDashboardResponse {

  @Schema(
    description = "Tổng số bài tập đã giải được trong toàn bộ khoảng thời gian (distinct exercises)",
    example = "25"
  )
  private Long totalSolved;

  @Schema(
    description = "Tổng số bài tập EASY đã giải được trong toàn bộ khoảng thời gian (distinct exercises, score >= 100)",
    example = "10"
  )
  private Long easyTotal;

  @Schema(
    description = "Tổng số bài tập MEDIUM đã giải được trong toàn bộ khoảng thời gian (distinct exercises, score >= 200)",
    example = "10"
  )
  private Long mediumTotal;

  @Schema(
    description = "Tổng số bài tập HARD đã giải được trong toàn bộ khoảng thời gian (distinct exercises, score >= 300)",
    example = "5"
  )
  private Long hardTotal;

  @Schema(description = "Danh sách thống kê theo từng ngày", example = "[]")
  private List<StudentSubmissionStatsResponseDTO> stats;
}
