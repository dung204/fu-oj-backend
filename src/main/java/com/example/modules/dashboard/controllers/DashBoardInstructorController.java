package com.example.modules.dashboard.controllers;

import com.example.base.dtos.SuccessResponseDTO;
import com.example.base.utils.AppRoutes;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.enums.Role;
import com.example.modules.dashboard.dtos.InstructorDashboardRequestDTO;
import com.example.modules.dashboard.dtos.InstructorDashboardStatsDTO;
import com.example.modules.dashboard.services.DashBoardInstructorService;
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
@RequestMapping(AppRoutes.DASHBOARD_INSTRUCTOR_PREFIX)
@RequiredArgsConstructor
@Tag(name = "Dashboard Instructor", description = "APIs cho dashboard của giảng viên")
@SecurityRequirement(name = "bearerAuth")
public class DashBoardInstructorController {

  private final DashBoardInstructorService dashBoardInstructorService;

  @GetMapping("/")
  @AllowRoles({ Role.INSTRUCTOR, Role.ADMIN })
  @Operation(
    summary = "Lấy thống kê dashboard cho instructor",
    description = """
    Lấy thống kê dashboard với khả năng filter theo groupId (optional).
    - Nếu có groupId: trả về thống kê của group đó
    - Nếu không có groupId: trả về thống kê tất cả groups

    Response bao gồm:
    - totalGroups: Tổng số groups
    - totalStudents: Tổng số students
    - totalExams: Tổng số exams
    - totalExercises: Tổng số exercises
    - examsComing: Số exams diễn ra hôm nay
    """
  )
  public SuccessResponseDTO<InstructorDashboardStatsDTO> getDashboardStats(
    @CurrentUser User currentUser,
    @ParameterObject @Valid InstructorDashboardRequestDTO request
  ) {
    InstructorDashboardStatsDTO data = dashBoardInstructorService.getDashboardStatsByGroup(
      currentUser.getId(),
      request
    );
    return SuccessResponseDTO.<InstructorDashboardStatsDTO>builder()
      .status(200)
      .message("Lấy thống kê dashboard thành công")
      .data(data)
      .build();
  }
}
