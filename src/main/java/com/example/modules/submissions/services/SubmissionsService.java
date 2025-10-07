package com.example.modules.submissions.services;

import com.example.modules.Judge0.services.Judge0Service;
import com.example.modules.Judge0.uitils.Base64Uitils;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.redis.configs.publishers.SubmissionPublisher;
import com.example.modules.submission_results.entities.SubmissionResult;
import com.example.modules.submission_results.repositories.SubmissionResultRepository;
import com.example.modules.submissions.dtos.SubmissionRequest;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.submissions.repositories.SubmissionsRepository;
import com.example.modules.test_cases.entities.TestCase;
import com.example.modules.test_cases.repositories.TestCasesRepository;
import com.example.modules.users.entities.User;
import com.example.modules.users.repositories.UsersRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionsService {

  private final Judge0Service judge0Service;
  private final SubmissionsRepository submissionRepository;
  private final TestCasesRepository testCaseRepository;
  private final SubmissionResultRepository submissionResultRepository;
  private final UsersRepository userRepository;
  private final ExercisesRepository exerciseRepository;
  private final SubmissionPublisher submissionPublisher;

  @Transactional
  public Submission createSubmission(SubmissionRequest request) {
    // get user + exercise
    User user = userRepository
      .findById(String.valueOf(request.getUserId()))
      .orElseThrow(() -> new RuntimeException("User not found"));

    Exercise exercise = exerciseRepository
      .findById(String.valueOf(request.getExerciseId()))
      .orElseThrow(() -> new RuntimeException("Exercise not found"));

    // create submission
    Submission submission = Submission.builder()
      .user(user)
      .exercise(exercise)
      .sourceCode(request.getSourceCode())
      .languageCode(request.getLanguageCode())
      .exerciseItem(exercise.getTitle())
      .time(null)
      .memory(null)
      .build();
    submission = submissionRepository.save(submission);

    // get all test cases of exercise
    List<TestCase> testCases = testCaseRepository.findAllByExerciseId((exercise.getId()));

    // bàn lại format lưu test case với ae sau
    List<String> testInputs = testCases.stream().map(TestCase::getInput).toList();

    // Gửi batch lên Judge0 -> nhận list token
    List<String> tokens = judge0Service.createBatchSubmission(
      request.getSourceCode(),
      request.getLanguageCode(),
      testInputs
    );

    // Gắn từng token với từng test case -> lưu SubmissionResult với verdict = IN_QUEUE
    for (int i = 0; i < testCases.size(); i++) {
      SubmissionResult result = SubmissionResult.builder()
        .submission(submission)
        .testCase(testCases.get(i))
        .token(tokens.get(i))
        .verdict("IN_QUEUE")
        .build();

      log.info("Submission {}, result: {} ", i, result.toString());
      submissionResultRepository.save(result);
    }

    log.info("Submission {} created with {} test cases", submission.getId(), testCases.size());
    return submission;
  }

  public Submission createSubmissionBase64(SubmissionRequest request) {
    // get user + exercise
    User user = userRepository
      .findById(String.valueOf(request.getUserId()))
      .orElseThrow(() -> new RuntimeException("User not found"));

    Exercise exercise = exerciseRepository
      .findById(String.valueOf(request.getExerciseId()))
      .orElseThrow(() -> new RuntimeException("Exercise not found"));

    // create submission
    Submission submission = Submission.builder()
      .user(user)
      .exercise(exercise)
      .sourceCode(request.getSourceCode())
      .languageCode(request.getLanguageCode())
      .exerciseItem(exercise.getTitle())
      .time(null)
      .memory(null)
      .build();
    submission = submissionRepository.save(submission);

    // get all test cases of exercise
    List<TestCase> testCases = testCaseRepository.findAllByExerciseId((exercise.getId()));

    // bàn lại format lưu test case với ae sau
    List<String> testInputs = testCases.stream().map(TestCase::getInput).toList();

    // Gửi batch lên Judge0 -> nhận list token
    List<String> tokens = judge0Service.createBatchSubmissionBase64(
      request.getSourceCode(),
      request.getLanguageCode(),
      testInputs
    );

    // Gắn từng token với từng test case -> lưu SubmissionResult với verdict = IN_QUEUE
    for (int i = 0; i < testCases.size(); i++) {
      SubmissionResult result = SubmissionResult.builder()
        .submission(submission)
        .testCase(testCases.get(i))
        .token(tokens.get(i))
        .verdict("IN_QUEUE")
        .build();

      log.info("Submission {}, result: {} ", i, result.toString());
      submissionResultRepository.save(result);
    }

    log.info("Submission {} created with {} test cases", submission.getId(), testCases.size());
    return submission;
  }

  /**
   * Khi Judge0 callback về, update từng test case
   * <p>
   * data: {
   * "stdout": "NQo=\n",
   * "time": "0.134",
   * "memory": 13856,
   * "stderr": null,
   * "token": "583b7296-9ac6-4136-a654-5904bc549b88",
   * "compile_output": null,
   * "message": null,
   * "status": {
   * "id": 4,
   * "description": "Wrong Answer"
   * }
   * }
   */
  @Transactional
  public void handleCallback(Map<String, Object> result) {
    log.info("Received callback result: {}", result);

    //1. Extract basic fields
    String token = (String) result.get("token");
    Map<String, Object> statusMap = (Map<String, Object>) result.get("status");
    int statusId = (int) statusMap.get("id");
    String statusDesc = (String) statusMap.get("description");

    String stdout = (String) result.get("stdout");
    String stderr = (String) result.get("stderr");

    //2. Decode base64 safely using Base64Utils
    String decodedStdout = Base64Uitils.decodeBase64Safe(stdout);
    String decodedStderr = Base64Uitils.decodeBase64Safe(stderr);

    if (decodedStderr != null && !decodedStderr.isEmpty()) {
      log.error("Decoded stderr: {}", decodedStderr);
      //      throw new RuntimeException("Decoded stderr: " + decodedStderr);
    }

    //3. Find submission result in DB
    SubmissionResult sr = submissionResultRepository
      .findByToken(token)
      .orElseThrow(() -> new RuntimeException("Unknown token: " + token));

    String expected = sr.getTestCase().getOutput() != null
      ? sr.getTestCase().getOutput().trim()
      : "";

    String actual = decodedStdout != null ? decodedStdout.trim() : "";

    //4. Determine verdict
    String verdict;
    switch (statusId) {
      case 6 -> verdict = "COMPILATION_ERROR";
      case 7 -> verdict = "RUNTIME_ERROR";
      case 5 -> verdict = "TIME_LIMIT_EXCEEDED";
      case 8 -> verdict = "INTERNAL_ERROR";
      default -> verdict = expected.equals(actual) ? "ACCEPTED" : "WRONG_ANSWER";
    }

    //5. Update submission result
    sr.setVerdict(verdict);
    sr.setActualOutput(decodedStdout);
    sr.setStderr(decodedStderr);
    submissionResultRepository.save(sr);

    //6. Publish Redis message
    Map<String, Object> message = Map.of(
      "submissionId",
      sr.getSubmission().getId(),
      "testCaseId",
      sr.getTestCase().getId(),
      "token",
      token,
      "verdict",
      verdict,
      "actualOutput",
      actual,
      "expectedOutput",
      expected
    );
    submissionPublisher.publishSubmissionUpdate(message);

    log.info("Callback for token {} => {}", token, verdict);

    // 7. Kiểm tra nếu toàn bộ test case đã xong
    Submission submission = sr.getSubmission();
    List<SubmissionResult> allResults = submissionResultRepository.findAllBySubmission(submission);

    boolean allDone = allResults.stream().allMatch(r -> !"IN_QUEUE".equals(r.getVerdict()));
    if (allDone) {
      String finalVerdict = allResults.stream().allMatch(r -> "ACCEPTED".equals(r.getVerdict()))
        ? "ACCEPTED"
        : allResults.stream().anyMatch(r -> r.getVerdict().equals("COMPILATION_ERROR"))
          ? "COMPILATION_ERROR"
          : allResults.stream().anyMatch(r -> r.getVerdict().equals("RUNTIME_ERROR"))
            ? "RUNTIME_ERROR"
            : allResults.stream().anyMatch(r -> r.getVerdict().equals("TIME_LIMIT_EXCEEDED"))
              ? "TIME_LIMIT_EXCEEDED"
              : "WRONG_ANSWER";

      submission.setTime("—");
      submission.setMemory("—");
      submission.setInput("auto");
      submission.setExerciseItem(submission.getExercise().getTitle());
      submissionRepository.save(submission);

      log.info("Submission {} finished with {}", submission.getId(), finalVerdict);
    }
  }
}
