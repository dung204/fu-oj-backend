package com.example.modules.exams.controllers;

import static com.example.base.utils.AppRoutes.EXAMS_PREFIX;

import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.enums.Role;
import com.example.modules.exams.dtos.ExamResultRequestDto;
import com.example.modules.exams.dtos.ExamResultResponseDto;
import com.example.modules.exams.dtos.ExamSubmissionCreateDto;
import com.example.modules.exams.dtos.ExamSubmissionResponseDto;
import com.example.modules.exams.services.ExamSubmissionService;
import com.example.modules.users.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping(path = EXAMS_PREFIX + "/submissions")
@RequiredArgsConstructor
@Tag(name = "Exam Submissions", description = "API for managing exam submissions")
public class ExamSubmissionController {

  private final ExamSubmissionService examSubmissionService;

  @AllowRoles(Role.STUDENT)
  @Operation(
    summary = "Submit an exercise solution for an exam (for STUDENT only)",
    description = """
    Submit source code for a specific exercise in an exam.

    **Preconditions:**
    - Current time must be between exam startTime and endTime
    - Student must be enrolled in the exam's group
    - Exercise must be part of the exam

    **Process:**
    - Creates a submission through the standard submission flow
    - Links the submission to the exam
    - Score will be calculated after Judge0 callback
    """,
    responses = {
      @ApiResponse(responseCode = "201", description = "Exam submission created successfully"),
      @ApiResponse(
        responseCode = "400",
        description = """
        - Exam has not started yet
        - Exam has already ended
        - Exercise is not part of the exam
        - Invalid request format
        """,
        content = @Content
      ),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = """
        - User's role is not STUDENT
        - Student is not enrolled in the exam's group
        """,
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Exam or exercise not found",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<ExamSubmissionResponseDto> createExamSubmission(
    @RequestBody @Valid ExamSubmissionCreateDto dto,
    @CurrentUser User currentUser
  ) {
    log.info(
      "Creating exam submission for user {} on exam {} exercise {}",
      currentUser.getId(),
      dto.getExamId(),
      dto.getExerciseId()
    );

    ExamSubmissionResponseDto response = examSubmissionService.createExamSubmission(
      dto,
      currentUser
    );

    return SuccessResponseDTO.<ExamSubmissionResponseDto>builder()
      .status(201)
      .message("Exam submission created successfully")
      .data(response)
      .build();
  }

  @AllowRoles({ Role.STUDENT, Role.INSTRUCTOR })
  @Operation(
    summary = "Get exam result for a specific student",
    description = """
    Retrieve all submissions and results for a student in a specific exam.

    **Response includes:**
    - Exam details (title, code, time range)
    - Student information
    - List of all submissions with scores
    - Overall statistics (total score, completed exercises)

    **Access:**
    - Students can view their own results
    - Instructors can view any student's results
    """,
    responses = {
      @ApiResponse(responseCode = "200", description = "Exam result retrieved successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Invalid request format",
        content = @Content
      ),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "404",
        description = "Exam or user not found",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/results")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<ExamResultResponseDto> getExamResult(
    @ParameterObject @Valid ExamResultRequestDto dto,
    @CurrentUser User currentUser
  ) {
    // Students can only view their own results
    if (
      currentUser.getAccount().getRole() == Role.STUDENT &&
      !currentUser.getId().equals(dto.getUserId())
    ) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "Students can only view their own exam results"
      );
    }

    log.info("Fetching exam result for exam {} and user {}", dto.getExamId(), dto.getUserId());

    ExamResultResponseDto response = examSubmissionService.getExamResult(dto);

    return SuccessResponseDTO.<ExamResultResponseDto>builder()
      .status(200)
      .message("Exam result retrieved successfully")
      .data(response)
      .build();
  }
}
