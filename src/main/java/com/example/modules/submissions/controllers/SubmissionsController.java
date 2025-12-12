package com.example.modules.submissions.controllers;

import static com.example.base.utils.AppRoutes.SUBMISSIONS_PREFIX;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.Judge0.dtos.Judge0SubmissionResponseDTO;
import com.example.modules.Judge0.services.Judge0Service;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.enums.Role;
import com.example.modules.submissions.dtos.RunCodeRequest;
import com.example.modules.submissions.dtos.RunCodeResponseDTO;
import com.example.modules.submissions.dtos.SubmissionRequest;
import com.example.modules.submissions.dtos.SubmissionResponseDTO;
import com.example.modules.submissions.dtos.SubmissionStatisticsRequestDTO;
import com.example.modules.submissions.dtos.SubmissionStatisticsResponseDTO;
import com.example.modules.submissions.dtos.SubmissionsSearchDTO;
import com.example.modules.submissions.services.SubmissionsService;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = SUBMISSIONS_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "submissions", description = "Operations related to submissions")
@RequiredArgsConstructor
public class SubmissionsController {

  private final SubmissionsService submissionsService;
  private final Judge0Service judge0Service;

  @Operation(
    summary = "Retrieve all existing submissions",
    responses = {
      @ApiResponse(responseCode = "200", description = "Topics retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping
  public PaginatedSuccessResponseDTO<SubmissionResponseDTO> getAllTopics(
    @ParameterObject @Valid SubmissionsSearchDTO submissionsSearchDTO
  ) {
    return PaginatedSuccessResponseDTO.<SubmissionResponseDTO>builder()
      .message("Lấy danh sách bài nộp thành công")
      .page(submissionsService.getAllSubmissions(submissionsSearchDTO))
      .filters(submissionsSearchDTO.getFilters())
      .build();
  }

  @AllowRoles({ Role.STUDENT, Role.INSTRUCTOR, Role.ADMIN })
  @Operation(
    summary = "Create a new submission with base64 encoded source code (for STUDENT only)",
    responses = {
      @ApiResponse(responseCode = "201", description = "Submission created successfully"),
      @ApiResponse(
        responseCode = "400",
        description = """
        - Exercise ID is empty or invalid
        - Source code is empty
        - Language code is empty or not supported
        """,
        content = @Content
      ),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = """
        - User's role is not `STUDENT`
        - Turnstile token is missing
        """,
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping
  // TODO: re-enable Turnstile
  //  @VerifyTurnstile
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<SubmissionResponseDTO> createSubmissionBase64(
    @RequestBody SubmissionRequest request,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<SubmissionResponseDTO>builder()
      .message("Nộp bài thành công")
      .data(submissionsService.createSubmissionBase64(request, currentUser))
      .build();
  }

  @Operation(
    summary = "Get submission result by token",
    responses = {
      @ApiResponse(responseCode = "200", description = "Get submission result successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Token is empty or invalid",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/result/{token}")
  public SuccessResponseDTO<Judge0SubmissionResponseDTO> GetSubmissionByToken(
    @PathVariable String token
  ) {
    Judge0SubmissionResponseDTO result = judge0Service.getSubmission(token);
    return SuccessResponseDTO.<Judge0SubmissionResponseDTO>builder()
      .message("Lấy kết quả bài nộp thành công")
      .data(result)
      .build();
  }

  @Operation(
    summary = "Run code without saving the result to database (for STUDENT only)",
    responses = {
      @ApiResponse(responseCode = "201", description = "Submission created successfully"),
      @ApiResponse(
        responseCode = "400",
        description = """
        - Exercise ID is empty or invalid
        - Source code is empty
        - Language code is empty or not supported
        """,
        content = @Content
      ),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = """
        - User's role is not `STUDENT`
        - Turnstile token is missing
        """,
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping("/run")
  @AllowRoles({ Role.STUDENT, Role.INSTRUCTOR, Role.ADMIN })
  public SuccessResponseDTO<RunCodeResponseDTO> runCode(
    @Valid @RequestBody RunCodeRequest request
  ) {
    RunCodeResponseDTO result = submissionsService.runCode(request);
    return SuccessResponseDTO.<RunCodeResponseDTO>builder()
      .message("Chạy code thành công")
      .data(result)
      .build();
  }

  @Operation(
    summary = "Get all submission results by submission ID (for STUDENT only)",
    responses = {
      @ApiResponse(responseCode = "200", description = "Get submission results successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Submission ID is empty or invalid",
        content = @Content
      ),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "403", description = "User is not a STUDENT", content = @Content),
      @ApiResponse(responseCode = "404", description = "Submission not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{submissionId}/result")
  public SuccessResponseDTO<SubmissionResponseDTO> getAllSubmissionResult(
    @PathVariable String submissionId
  ) {
    SubmissionResponseDTO result = submissionsService.getAllSubmissionResultBySubmissionId(
      submissionId
    );
    return SuccessResponseDTO.<SubmissionResponseDTO>builder()
      .message("Lấy kết quả các bài nộp thành công")
      .data(result)
      .build();
  }

  @GetMapping("/statistics")
  public SuccessResponseDTO<SubmissionStatisticsResponseDTO> getSubmissionStatistics(
    @ParameterObject @Valid SubmissionStatisticsRequestDTO requestDTO
  ) {
    return SuccessResponseDTO.<SubmissionStatisticsResponseDTO>builder()
      .message("Lấy thống kê bài nộp thành công")
      .data(submissionsService.getSubmissionStatistics(requestDTO))
      .build();
  }
}
