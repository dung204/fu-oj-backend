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
public class InstructorDashboardRequestDTO {

  @Schema(description = "Group ID để filter thống kê (optional)")
  private String groupId;
}
