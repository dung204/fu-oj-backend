package com.example.modules.submissions.controllers;

import static com.example.base.utils.AppRoutes.SUBMISSIONS_PREFIX;

import com.example.modules.submissions.dtos.SubmissionRequest;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.submissions.services.SubmissionsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = SUBMISSIONS_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "submissions", description = "Operations related to submissions")
@RequiredArgsConstructor
public class SubmissionsController {

  private final SubmissionsService submissionServices;

  @PostMapping
  public ResponseEntity<?> createSubmission(@RequestBody SubmissionRequest request) {
    Submission submission = submissionServices.createSubmission(request);
    return ResponseEntity.ok(submission);
  }

  @PostMapping("/callback")
  public ResponseEntity<Void> handleCallback(@RequestBody Map<String, Object> result) {
    log.info("Received callback from Judge0: {}", result);
    submissionServices.handleCallback(result);
    return ResponseEntity.ok().build();
  }
}
