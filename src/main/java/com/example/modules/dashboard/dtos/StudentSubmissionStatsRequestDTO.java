package com.example.modules.dashboard.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSubmissionStatsRequestDTO {

  @NotNull(message = "startDate is required")
  @Schema(description = "Ngày bắt đầu để thống kê (format: YYYY-MM-DD)", example = "2024-11-01")
  private LocalDate startDate;

  @NotNull(message = "endDate is required")
  @Schema(description = "Ngày kết thúc để thống kê (format: YYYY-MM-DD)", example = "2024-11-30")
  private LocalDate endDate;

  @Schema(
    description = "ID của sinh viên để filter (chỉ ADMIN mới dùng được). Nếu null, thống kê tất cả sinh viên",
    example = "550e8400-e29b-41d4-a716-446655440000"
  )
  private String studentId;
}
