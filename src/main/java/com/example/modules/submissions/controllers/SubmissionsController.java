package com.example.modules.submissions.controllers;

import static com.example.base.utils.AppRoutes.SUBMISSIONS_PREFIX;

import com.example.base.annotations.VerifyTurnstile;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.Judge0.dtos.Judge0CallbackRequestDTO;
import com.example.modules.Judge0.dtos.Judge0SubmissionResponseDTO;
import com.example.modules.Judge0.services.Judge0Service;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.annotations.Public;
import com.example.modules.auth.enums.Role;
import com.example.modules.submission_results.dtos.SubmissionResultResponseDTO;
import com.example.modules.submissions.dtos.RunCodeRequest;
import com.example.modules.submissions.dtos.RunCodeResponseDTO;
import com.example.modules.submissions.dtos.SubmissionRequest;
import com.example.modules.submissions.dtos.SubmissionResponseDTO;
import com.example.modules.submissions.services.SubmissionsService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = SUBMISSIONS_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "submissions", description = "Operations related to submissions")
@RequiredArgsConstructor
public class SubmissionsController {

  private final SubmissionsService submissionsService;
  private final Judge0Service judge0Service;

  @AllowRoles(Role.STUDENT)
  @Operation(
    summary = "Create a new submission (for STUDENT only)",
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
        description = "User's role is not `STUDENT`",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<SubmissionResponseDTO> createSubmission(
    @RequestBody SubmissionRequest request,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<SubmissionResponseDTO>builder()
      .message("Submission created successfully")
      .data(submissionsService.createSubmission(request, currentUser))
      .build();
  }

  @AllowRoles(Role.STUDENT)
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
        description = "User's role is not `STUDENT`",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping("/base64")
  @VerifyTurnstile
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<SubmissionResponseDTO> createSubmissionBase64(
    @RequestBody SubmissionRequest request,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<SubmissionResponseDTO>builder()
      .message("Submission created successfully")
      .data(submissionsService.createSubmissionBase64(request, currentUser))
      .build();
  }

  @Public
  @Operation(summary = "Handle callback from Judge0")
  @PutMapping("/callback")
  public ResponseEntity<Void> handleCallback(@RequestBody Judge0CallbackRequestDTO callback) {
    log.info("Received callback from Judge0: {}", callback);
    submissionsService.handleCallback(callback);
    return ResponseEntity.ok().build();
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
      .message("Get submission result successfully")
      .data(result)
      .build();
  }

  @PostMapping("/{submissionId}/calculate")
  @Public
  public SuccessResponseDTO<SubmissionResponseDTO> calculateTestCasesPassed(
    @PathVariable String submissionId
  ) {
    return SuccessResponseDTO.<SubmissionResponseDTO>builder()
      .message("Calculating test cases passed for submission successfully")
      .data(submissionsService.calculateTestCasesPassed(submissionId))
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
        description = "User's role is not `STUDENT`",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping("/run")
  @AllowRoles(Role.STUDENT)
  public SuccessResponseDTO<RunCodeResponseDTO> runCode(
    @Valid @RequestBody RunCodeRequest request
  ) {
    RunCodeResponseDTO result = submissionsService.runCode(request);
    return SuccessResponseDTO.<RunCodeResponseDTO>builder()
      .message("Run code successfully")
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
  public SuccessResponseDTO<List<SubmissionResultResponseDTO>> getAllSubmissionResult(
    @PathVariable String submissionId
  ) {
    List<SubmissionResultResponseDTO> result =
      submissionsService.getAllSubmissionResultBySubmissionId(submissionId);
    return SuccessResponseDTO.<List<SubmissionResultResponseDTO>>builder()
      .message("Run code successfully")
      .data(result)
      .build();
  }
}
