package com.example.modules.exams.controllers;

import static com.example.base.utils.AppRoutes.EXAMS_PREFIX;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.enums.Role;
import com.example.modules.exams.dtos.ExamCreateDTO;
import com.example.modules.exams.dtos.ExamResponseDTO;
import com.example.modules.exams.dtos.ExamsSearchDTO;
import com.example.modules.exams.services.ExamService;
import com.example.modules.exams.utils.ExamMapper;
import com.example.modules.users.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = EXAMS_PREFIX)
@RequiredArgsConstructor
@Tag(name = "Exams", description = "API for managing exams")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamController {

  ExamService examService;
  ExamMapper examMapper;

  @AllowRoles({ Role.INSTRUCTOR })
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
    @RequestBody @Valid ExamCreateDTO examCreateDTO,
    @CurrentUser User currentUser
  ) {
    List<ExamResponseDTO> createdExams = examService.createExamsForMultipleGroups(
      examCreateDTO,
      currentUser
    );
    return SuccessResponseDTO.<List<ExamResponseDTO>>builder()
      .status(201)
      .message("Exams created successfully for " + createdExams.size() + " group(s)")
      .data(createdExams)
      .build();
  }

  @Operation(
    summary = "Get exam by ID",
    description = "Access is determined by the user's role:\n" +
      "  * `ADMIN`: Can retrieve any exam.\n" +
      "  * `INSTRUCTOR`: Can retrieve any exam they created.\n" +
      "  * `STUDENT`: Can retrieve any exam of his joined group.\n",
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
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<ExamResponseDTO>builder()
      .status(200)
      .message("Exam retrieved successfully")
      .data(examMapper.toExamResponseDTO(examService.getExamById(id, currentUser)))
      .build();
  }

  @AllowRoles({ Role.INSTRUCTOR, Role.ADMIN })
  @Operation(
    summary = "Get all exams",
    description = "The returned exams list depends on the role of the authenticated user:\n" +
      "  * `ADMIN`: Returns all exams in the system.\n" +
      "  * `INSTRUCTOR`: Returns all exams created by the instructor.\n" +
      "  * `STUDENT`: Returns all exams of the joined groups\n",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exams retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public PaginatedSuccessResponseDTO<ExamResponseDTO> getAllExams(
    @ParameterObject @Valid ExamsSearchDTO examsSearchDTO,
    @CurrentUser User currentUser
  ) {
    return PaginatedSuccessResponseDTO.<ExamResponseDTO>builder()
      .status(200)
      .message("Exams retrieved successfully")
      .page(examService.getAllExams(examsSearchDTO, currentUser))
      .filters(examsSearchDTO.getFilters())
      .build();
  }

  @AllowRoles({ Role.INSTRUCTOR, Role.ADMIN })
  @Operation(
    summary = "Delete exam by ID (for INSTRUCTOR only)",
    description = "Can only soft delete an exam created by the instructor",
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
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<ExamResponseDTO>builder()
      .status(200)
      .message("Exam deleted successfully")
      .data(examService.deleteExam(id, currentUser))
      .build();
  }
}
