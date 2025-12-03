package com.example.modules.exams.controllers;

import static com.example.base.utils.AppRoutes.EXAMS_PREFIX;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.enums.Role;
import com.example.modules.exams.dtos.ExamCreateDTO;
import com.example.modules.exams.dtos.ExamResponseDTO;
import com.example.modules.exams.dtos.ExamUpdateDTO;
import com.example.modules.exams.dtos.ExamsSearchDTO;
import com.example.modules.exams.dtos.StudentExamProgressDTO;
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
@Tag(name = "exams", description = "API for managing exams")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamController {

  ExamService examService;
  ExamMapper examMapper;

  @AllowRoles({ Role.INSTRUCTOR, Role.ADMIN })
  @Operation(
    summary = "Create exams for multiple groups (for INSTRUCTOR only)",
    description = "Creates exams for multiple groups at once based on a single template. For each `groupId` provided, a new exam is generated with a unique title formatted as `{original title} {group name}`.\n\n" +
      "The `status` can be `DRAFT` or `UPCOMING` (defaults to `DRAFT`).\n\n" +
      "If the status is set to `UPCOMING`, the `startTime` must be at least 5 minutes in the future.",
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
  public SuccessResponseDTO<ExamResponseDTO> createExam(
    @RequestBody @Valid ExamCreateDTO examCreateDTO
  ) {
    ExamResponseDTO createdExam = examService.createExamForMultipleGroups(examCreateDTO);
    return SuccessResponseDTO.<ExamResponseDTO>builder()
      .status(201)
      .message("Exam created successfully for " + examCreateDTO.getGroupIds().size() + " group(s)")
      .data(createdExam)
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
    summary = "Update an exam by ID (for INSTRUCTOR or ADMIN)",
    description = "Updates an existing exam. An `INSTRUCTOR` can only update exams they created. An `ADMIN` can update any exam. Only the fields provided in the request body will be updated.\n\n" +
      "This endpoint does not update the `status` of exams. It can be only updated via the scheduled task or the `[PATCH] /api/v1/exams/{id}/publish`\n\n" +
      "An exam can not be updated when the status is `ONGOING`, `COMPLETED`, `CANCELLED`",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exam updated successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Invalid request body or parameters",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Exam not found, or user is not authorized to update this exam",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "409",
        description = "Exam not can not be updated because of the status",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PatchMapping("/{id}")
  public SuccessResponseDTO<ExamResponseDTO> updateExam(
    @PathVariable String id,
    @RequestBody @Valid ExamUpdateDTO examUpdateDTO,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<ExamResponseDTO>builder()
      .status(200)
      .message("Exam updated successfully.")
      .data(examService.updateExam(id, examUpdateDTO, currentUser))
      .build();
  }

  @AllowRoles({ Role.INSTRUCTOR, Role.ADMIN })
  @Operation(
    summary = "Publish a draft exam (for INSTRUCTOR or ADMIN)",
    description = "Changes an exam's status from `DRAFT` to `UPCOMING`. This makes the exam visible and schedules it to become `ONGOING` at its start time.\n\n" +
      "**Conditions for publishing:**\n" +
      "1. The exam must currently be in `DRAFT` status.\n" +
      "2. The exam's start time must be at least 5 minutes in the future.\n" +
      "3. An `INSTRUCTOR` can only publish exams they created.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exam published successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Cannot publish exam because its status is not DRAFT or its start time is invalid.",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Exam not found, or user is not authorized to the exam",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "409",
        description = "Exam not can not be published because the status is not `DRAFT`",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PatchMapping("/{id}/publish")
  public SuccessResponseDTO<ExamResponseDTO> publishExam(
    @PathVariable String id,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<ExamResponseDTO>builder()
      .status(200)
      .message("Exam published successfully.")
      .data(examService.publishExam(id, currentUser))
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

  @AllowRoles({ Role.INSTRUCTOR, Role.ADMIN })
  @Operation(
    summary = "Get student exam progress by exam ID and group ID",
    description = "Retrieves the progress information of all students in a group for a specific exam.\n\n" +
      "Returns information including:\n" +
      "- Student information (name, roll number, email)\n" +
      "- Total score from ExamRanking\n" +
      "- Whether the student has joined the exam (has ExamRanking)\n" +
      "- Whether the exam is completed (ExamRanking.completed = true)\n" +
      "- Progress for each exercise (score, submission status)\n\n" +
      "**Access Control:**\n" +
      "- `INSTRUCTOR`: Can only access groups they own\n" +
      "- `ADMIN`: Can access any group",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "Student exam progress retrieved successfully"
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Exam not found in the specified group",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "403",
        description = "Access denied - not authorized to view this group",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{examId}/groups/{groupId}/students-progress")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<List<StudentExamProgressDTO>> getStudentExamProgress(
    @PathVariable String examId,
    @PathVariable String groupId,
    @CurrentUser User currentUser
  ) {
    List<StudentExamProgressDTO> progress = examService.getStudentExamProgress(
      examId,
      groupId,
      currentUser
    );
    return SuccessResponseDTO.<List<StudentExamProgressDTO>>builder()
      .status(200)
      .message("Student exam progress retrieved successfully")
      .data(progress)
      .build();
  }

  @AllowRoles({ Role.INSTRUCTOR, Role.ADMIN })
  @Operation(
    summary = "Toggle exam examined status (for INSTRUCTOR or ADMIN)",
    description = "Toggles the `isExamined` status of an exam between `true` and `false`.\n\n" +
      "This field indicates whether the exam has been reviewed/graded by the instructor.\n\n" +
      "**Access Control:**\n" +
      "- `INSTRUCTOR`: Can only toggle status for exams they created\n" +
      "- `ADMIN`: Can toggle status for any exam",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exam examined status toggled successfully"),
      @ApiResponse(
        responseCode = "404",
        description = "Exam not found or user is not authorized",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PatchMapping("/{id}/toggle-examined")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<ExamResponseDTO> toggleExamExaminedStatus(
    @PathVariable String id,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<ExamResponseDTO>builder()
      .status(200)
      .message("Exam examined status toggled successfully")
      .data(examService.toggleExamExaminedStatus(id, currentUser))
      .build();
  }
}
