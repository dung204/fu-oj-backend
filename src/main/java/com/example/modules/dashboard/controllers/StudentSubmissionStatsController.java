package com.example.modules.dashboard.controllers;

import com.example.base.dtos.SuccessResponseDTO;
import com.example.base.utils.AppRoutes;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.enums.Role;
import com.example.modules.dashboard.dtos.StudentSubmissionDashboardResponse;
import com.example.modules.dashboard.dtos.StudentSubmissionStatsRequestDTO;
import com.example.modules.dashboard.services.StudentSubmissionStatsService;
import com.example.modules.users.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppRoutes.DASHBOARD_STUDENT_SUBMISSION_STATS_PREFIX)
@RequiredArgsConstructor
@Tag(
  name = "Dashboard Student Submission Stats",
  description = "APIs thống kê bài nộp của sinh viên"
)
@SecurityRequirement(name = "bearerAuth")
public class StudentSubmissionStatsController {

  private final StudentSubmissionStatsService studentSubmissionStatsService;

  @GetMapping
  @AllowRoles({ Role.STUDENT, Role.INSTRUCTOR, Role.ADMIN })
  @Operation(
    summary = "Thống kê bài nộp của sinh viên",
    description = """
    Lấy thống kê bài nộp của sinh viên theo date range.

    - **STUDENT**: Chỉ xem được thống kê của chính mình (studentId sẽ bị ignore nếu có)
    - **ADMIN**: Xem được thống kê của tất cả sinh viên hoặc filter theo studentId

    Response bao gồm:
    - totalSolved: Tổng số bài tập đã giải được trong toàn bộ khoảng thời gian (distinct exercises)
    - easyTotal: Tổng số bài tập EASY đã giải được trong toàn bộ khoảng thời gian (distinct exercises, score >= 100)
    - mediumTotal: Tổng số bài tập MEDIUM đã giải được trong toàn bộ khoảng thời gian (distinct exercises, score >= 200)
    - hardTotal: Tổng số bài tập HARD đã giải được trong toàn bộ khoảng thời gian (distinct exercises, score >= 300)
    - stats: Danh sách thống kê theo từng ngày, mỗi ngày có:
      - date: Ngày thống kê (YYYY-MM-DD)
      - totalSolved: Tổng số bài tập đã giải được trong ngày (distinct exercises)
      - easy: Số bài EASY đã giải (score >= 100)
      - medium: Số bài MEDIUM đã giải (score >= 200)
      - hard: Số bài HARD đã giải (score >= 300)

    Lưu ý:
    - Date range tối đa 90 ngày
    - Chỉ tính các submission đã accepted và không phải bài thi
    - Một bài tập chỉ đếm 1 lần trong mỗi ngày (distinct exercise)
    """
  )
  public SuccessResponseDTO<StudentSubmissionDashboardResponse> getSubmissionStats(
    @CurrentUser User currentUser,
    @ParameterObject @Valid StudentSubmissionStatsRequestDTO request
  ) {
    StudentSubmissionDashboardResponse stats =
      studentSubmissionStatsService.getStudentSubmissionStats(request, currentUser);
    return SuccessResponseDTO.<StudentSubmissionDashboardResponse>builder()
      .status(200)
      .message("Lấy thống kê bài nộp thành công")
      .data(stats)
      .build();
  }
}
