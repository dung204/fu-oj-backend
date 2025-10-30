package com.example.modules.exams.controllers;

import static com.example.base.utils.AppRoutes.EXAMS_PREFIX;

import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.enums.Role;
import com.example.modules.exams.dtos.ExamCreateDto;
import com.example.modules.exams.dtos.ExamResponseDTO;
import com.example.modules.exams.services.ExamService;
import com.example.modules.users.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = EXAMS_PREFIX)
@RequiredArgsConstructor
@Tag(name = "Exams", description = "API for managing exams")
public class ExamController {

  private final ExamService examService;

  @AllowRoles(Role.INSTRUCTOR)
  @Operation(
    summary = "Create exams for multiple groups (for INSTRUCTOR only)",
    description = "Create exams for multiple groups at once. For each group, an exam will be created with title '{original title} {group name}'",
    responses = {
      @ApiResponse(responseCode = "201", description = "Exams created successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Invalid request format",
        content = @Content
      ),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
      @ApiResponse(responseCode = "404", description = "Group not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<List<ExamResponseDTO>> createExams(
    @RequestBody @Valid ExamCreateDto examCreateDto,
    @CurrentUser User currentUser
  ) {
    List<ExamResponseDTO> createdExams = examService.createExamsForMultipleGroups(
      examCreateDto,
      currentUser
    );
    return SuccessResponseDTO.<List<ExamResponseDTO>>builder()
      .status(201)
      .message("Exams created successfully for " + createdExams.size() + " group(s)")
      .data(createdExams)
      .build();
  }

  @AllowRoles({ Role.INSTRUCTOR, Role.STUDENT })
  @Operation(
    summary = "Get exam by ID",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exam retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Exam not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<ExamResponseDTO> getExamById(
    @PathVariable String id,
    @CurrentUser User user
  ) {
    return SuccessResponseDTO.<ExamResponseDTO>builder()
      .status(200)
      .message("Exam retrieved successfully")
      .data(examService.getExamById(id))
      .build();
  }

  @AllowRoles({ Role.INSTRUCTOR, Role.ADMIN })
  @Operation(
    summary = "Get all exams",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exams retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<List<ExamResponseDTO>> getAllExams(@CurrentUser User user) {
    return SuccessResponseDTO.<List<ExamResponseDTO>>builder()
      .status(200)
      .message("Exams retrieved successfully")
      .data(examService.getAllExams())
      .build();
  }

  @AllowRoles(Role.INSTRUCTOR)
  @Operation(
    summary = "Delete exam by ID (for INSTRUCTOR only)",
    description = "Soft delete an exam",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exam deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Exam not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<ExamResponseDTO> deleteExam(
    @PathVariable String id,
    @CurrentUser User user
  ) {
    return SuccessResponseDTO.<ExamResponseDTO>builder()
      .status(200)
      .message("Exam deleted successfully")
      .data(examService.deleteExam(id))
      .build();
  }
}
