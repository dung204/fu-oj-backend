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

  @Schema(description = "Danh sách thống kê theo từng ngày", example = "[]")
  private List<StudentSubmissionStatsResponseDTO> stats;
}
