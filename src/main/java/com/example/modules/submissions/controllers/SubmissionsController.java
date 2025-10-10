package com.example.modules.submissions.controllers;

import static com.example.base.utils.AppRoutes.SUBMISSIONS_PREFIX;

import com.example.modules.Judge0.dtos.Judge0CallbackRequestDTO;
import com.example.modules.Judge0.dtos.Judge0SubmissionResponseDTO;
import com.example.modules.Judge0.services.Judge0Service;
import com.example.modules.auth.annotations.Public;
import com.example.modules.submission_results.dtos.SubmissionResultResponseDTO;
import com.example.modules.submissions.dtos.RunCodeRequest;
import com.example.modules.submissions.dtos.RunCodeResponseDTO;
import com.example.modules.submissions.dtos.SubmissionRequest;
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
  public ResponseEntity<?> createSubmission(@RequestBody SubmissionRequest request) {
    Submission submission = submissionServices.createSubmission(request);
    return ResponseEntity.ok(submission);
  }

  @PostMapping("/base64")
  @Public
  public ResponseEntity<?> createSubmissionBase64(@RequestBody SubmissionRequest request) {
    Submission submission = submissionServices.createSubmissionBase64(request);
    return ResponseEntity.ok(submission);
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
  public ResponseEntity<Judge0SubmissionResponseDTO> GetSubmissionByToken(
    @PathVariable String token
  ) {
    log.info("Received token: {}", token);
    Judge0SubmissionResponseDTO result = judge0Service.getSubmission(token);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/{submissionId}/calculate")
  @Public
  public ResponseEntity<Submission> calculateTestCasesPassed(@PathVariable String submissionId) {
    log.info("Calculating test cases passed for submission: {}", submissionId);
    Submission submission = submissionServices.calculateTestCasesPassed(submissionId);
    return ResponseEntity.ok(submission);
  }

  @PostMapping("/run")
  @Public
  public ResponseEntity<RunCodeResponseDTO> runCode(@Valid @RequestBody RunCodeRequest request) {
    log.info("Running code for exercise {} without saving to database", request.getExerciseId());
    RunCodeResponseDTO result = submissionServices.runCode(request);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{submissionId}/submissionResult")
  @Public
  public ResponseEntity<List<SubmissionResultResponseDTO>> getAllSubmissionResult(
    @PathVariable String submissionId
  ) {
    log.info("SubmissionId", submissionId);
    List<SubmissionResultResponseDTO> result =
      submissionServices.getAllSubmissionResultBySubmissionId(submissionId);
    return ResponseEntity.ok(result);
  }
}
