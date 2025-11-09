package com.example.modules.dashboard.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorDashboardStatsDTO {

  private Long totalStudents;
  private Long totalGroups;
  private Long totalExams;
  private Long totalExercises;
  private Long examsComing; // Số bài kiểm tra diễn ra hôm nay
}
