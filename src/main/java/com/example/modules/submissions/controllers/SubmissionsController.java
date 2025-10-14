package com.example.modules.submissions.controllers;

import static com.example.base.utils.AppRoutes.SUBMISSIONS_PREFIX;

import com.example.base.annotations.VerifyTurnstile;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.Judge0.dtos.Judge0CallbackRequestDTO;
import com.example.modules.Judge0.dtos.Judge0SubmissionResponseDTO;
import com.example.modules.Judge0.services.Judge0Service;
import com.example.modules.auth.annotations.Public;
import com.example.modules.submission_results.dtos.SubmissionResultResponseDTO;
import com.example.modules.submissions.dtos.RunCodeRequest;
import com.example.modules.submissions.dtos.RunCodeResponseDTO;
import com.example.modules.submissions.dtos.SubmissionRequest;
import com.example.modules.submissions.dtos.SubmissionResponseDTO;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.submissions.services.SubmissionsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = SUBMISSIONS_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "submissions", description = "Operations related to submissions")
@RequiredArgsConstructor
public class SubmissionsController {

  private final SubmissionsService submissionServices;
  private final Judge0Service judge0Service;

  @PostMapping
  @Public
  public SuccessResponseDTO<SubmissionResponseDTO> createSubmission(
    @RequestBody SubmissionRequest request
  ) {
    Submission submission = submissionServices.createSubmission(request);
    SubmissionResponseDTO submissionResponseDTO = SubmissionResponseDTO.fromEntity(submission);
    return SuccessResponseDTO.<SubmissionResponseDTO>builder()
      .message("Submission created successfully")
      .data(submissionResponseDTO)
      .build();
  }

  @PostMapping("/base64")
  @Public
  @VerifyTurnstile
  public SuccessResponseDTO<SubmissionResponseDTO> createSubmissionBase64(
    @RequestBody SubmissionRequest request
  ) {
    Submission submission = submissionServices.createSubmissionBase64(request);
    SubmissionResponseDTO submissionResponseDTO = SubmissionResponseDTO.fromEntity(submission);
    return SuccessResponseDTO.<SubmissionResponseDTO>builder()
      .message("Submission created successfully")
      .data(submissionResponseDTO)
      .build();
  }

  @PutMapping("/callback")
  @Public
  public ResponseEntity<Void> handleCallback(@RequestBody Judge0CallbackRequestDTO callback) {
    log.info("Received callback from Judge0: {}", callback);
    submissionServices.handleCallback(callback);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/result/{token}")
  @Public
  public SuccessResponseDTO<Judge0SubmissionResponseDTO> GetSubmissionByToken(
    @PathVariable String token
  ) {
    log.info("Received token: {}", token);
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
    log.info("Calculating test cases passed for submission: {}", submissionId);
    Submission submission = submissionServices.calculateTestCasesPassed(submissionId);
    SubmissionResponseDTO submissionResponseDTO = SubmissionResponseDTO.fromEntity(submission);
    return SuccessResponseDTO.<SubmissionResponseDTO>builder()
      .message("Calculating test cases passed for submission successfully")
      .data(submissionResponseDTO)
      .build();
  }

  @PostMapping("/run")
  @Public
  public SuccessResponseDTO<RunCodeResponseDTO> runCode(
    @Valid @RequestBody RunCodeRequest request
  ) {
    log.info("Running code for exercise {} without saving to database", request.getExerciseId());
    RunCodeResponseDTO result = submissionServices.runCode(request);
    return SuccessResponseDTO.<RunCodeResponseDTO>builder()
      .message("Run code successfully")
      .data(result)
      .build();
  }

  @GetMapping("/{submissionId}/submissionResult")
  @Public
  public SuccessResponseDTO<List<SubmissionResultResponseDTO>> getAllSubmissionResult(
    @PathVariable String submissionId
  ) {
    log.info("SubmissionId", submissionId);
    List<SubmissionResultResponseDTO> result =
      submissionServices.getAllSubmissionResultBySubmissionId(submissionId);
    return SuccessResponseDTO.<List<SubmissionResultResponseDTO>>builder()
      .message("Run code successfully")
      .data(result)
      .build();
  }
}
