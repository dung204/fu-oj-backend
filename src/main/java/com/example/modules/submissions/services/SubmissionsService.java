package com.example.modules.submissions.services;

import com.example.modules.Judge0.enums.Judge0Status;
import com.example.modules.Judge0.services.Judge0Service;
import com.example.modules.Judge0.uitils.Base64Uitils;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.redis.configs.publishers.SubmissionPublisher;
import com.example.modules.submission_results.dtos.SubmissionResultResponseDTO;
import com.example.modules.submission_results.entities.SubmissionResult;
import com.example.modules.submission_results.repositories.SubmissionResultRepository;
import com.example.modules.submissions.dtos.Judge0StatusDTO;
import com.example.modules.submissions.dtos.RunCodeRequest;
import com.example.modules.submissions.dtos.RunCodeResponseDTO;
import com.example.modules.submissions.dtos.SubmissionRequest;
import com.example.modules.submissions.dtos.TestCaseResultDTO;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.submissions.repositories.SubmissionsRepository;
import com.example.modules.submissions.utils.SubmissionResultMapper;
import com.example.modules.test_cases.entities.TestCase;
import com.example.modules.test_cases.repositories.TestCasesRepository;
import com.example.modules.users.entities.User;
import com.example.modules.users.repositories.UsersRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
  private final SubmissionResultMapper submissionResultMapper;

  @Transactional
  public Submission createSubmission(SubmissionRequest request) {
    // get user + exercise
    User user = userRepository
      .findById(String.valueOf(request.getUserId()))
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    Exercise exercise = exerciseRepository
      .findById(String.valueOf(request.getExerciseId()))
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));

    // get all test cases of exercise
    List<TestCase> testCases = testCaseRepository.findAllByExerciseId((exercise.getId()));

    // create submission
    Submission submission = Submission.builder()
      .user(user)
      .exercise(exercise)
      .sourceCode(request.getSourceCode())
      .languageCode(request.getLanguageCode())
      .exerciseItem(exercise.getTitle())
      .time(null)
      .memory(null)
      .passedTestCases(0)
      .totalTestCases(testCases.size())
      .build();
    submission = submissionRepository.save(submission);

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
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    Exercise exercise = exerciseRepository
      .findById(String.valueOf(request.getExerciseId()))
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));

    // get all test cases of exercise
    List<TestCase> testCases = testCaseRepository.findAllByExerciseId((exercise.getId()));

    // create submission
    Submission submission = Submission.builder()
      .user(user)
      .exercise(exercise)
      .sourceCode(request.getSourceCode())
      .languageCode(request.getLanguageCode())
      .exerciseItem(exercise.getTitle())
      .time(null)
      .memory(null)
      .passedTestCases(0)
      .totalTestCases(testCases.size())
      .build();
    submission = submissionRepository.save(submission);

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
        .verdict(String.valueOf(Judge0Status.IN_QUEUE))
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
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown token: " + token)
      );

    String expected = sr.getTestCase().getOutput() != null
      ? sr.getTestCase().getOutput().trim()
      : "";

    String actual = decodedStdout != null ? decodedStdout.trim() : "";

    //4. Determine verdict
    String verdict;
    switch (statusId) {
      case 1 -> verdict = String.valueOf(Judge0Status.IN_QUEUE);
      case 2 -> verdict = String.valueOf(Judge0Status.PROCESSING);
      //      case 3 -> verdict = String.valueOf(Judge0Status.ACCEPTED);
      //      case 4 -> verdict = String.valueOf(Judge0Status.WRONG_ANSWER);
      case 5 -> verdict = String.valueOf(Judge0Status.TIME_LIMIT_EXCEEDED);
      case 6 -> verdict = String.valueOf(Judge0Status.COMPILATION_ERROR);
      case 7, 8, 9, 10, 11, 12 -> verdict = String.valueOf(Judge0Status.RUNTIME_ERROR);
      case 13 -> verdict = String.valueOf(Judge0Status.INTERNAL_ERROR);
      case 14 -> verdict = String.valueOf(Judge0Status.EXEC_FORMAT_ERROR);
      default -> verdict = (expected != null && expected.equals(actual))
        ? String.valueOf(Judge0Status.ACCEPTED)
        : String.valueOf(Judge0Status.WRONG_ANSWER);
    }

    //5. Update submission result
    sr.setVerdict(verdict);
    sr.setActualOutput(decodedStdout);
    sr.setStderr(decodedStderr);
    submissionResultRepository.save(sr);

    //6. Publish Redis message -> send WebSocket to FE (via SubmissionSubscriber)
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
      expected,
      "userId",
      sr.getSubmission().getUser().getId()
    );
    submissionPublisher.publishSubmissionUpdate(message);

    log.info("Callback for token {} => {}", token, verdict);
  }

  @Transactional
  public Submission calculateTestCasesPassed(String submissionId) {
    Submission submission = submissionRepository
      .findById(submissionId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));

    List<SubmissionResult> allResults = submissionResultRepository.findAllBySubmission(submission);

    // Đếm số test case đúng
    long passedCount = allResults
      .stream()
      .filter(r -> "ACCEPTED".equals(r.getVerdict()))
      .count();

    // Xác định verdict cuối cùng
    String finalVerdict = allResults.stream().allMatch(r -> "ACCEPTED".equals(r.getVerdict()))
      ? "ACCEPTED"
      : allResults.stream().anyMatch(r -> r.getVerdict().equals("COMPILATION_ERROR"))
        ? "COMPILATION_ERROR"
        : allResults.stream().anyMatch(r -> r.getVerdict().equals("RUNTIME_ERROR"))
          ? "RUNTIME_ERROR"
          : allResults.stream().anyMatch(r -> r.getVerdict().equals("TIME_LIMIT_EXCEEDED"))
            ? "TIME_LIMIT_EXCEEDED"
            : "WRONG_ANSWER";

    // Cập nhật submission
    submission.setTime("—");
    submission.setMemory("—");
    submission.setExerciseItem(submission.getExercise().getTitle());
    submission.setPassedTestCases((int) passedCount);
    submission.setTotalTestCases(allResults.size());
    submissionRepository.save(submission);

    log.info(
      "Submission {} finished with {} ({}/{} test cases passed)",
      submission.getId(),
      finalVerdict,
      passedCount,
      allResults.size()
    );

    return submission;
  }

  public RunCodeResponseDTO runCode(RunCodeRequest request) {
    log.info("Running code for exercise: {}", request.getExerciseId());

    // 1. Lấy exercise từ DB
    Exercise exercise = exerciseRepository
      .findById(request.getExerciseId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));

    // 2. Lấy tất cả test cases public
    List<TestCase> publicTestCases = testCaseRepository.findAllByExerciseIdAndIsPublicTrue(
      exercise.getId()
    );

    if (publicTestCases.isEmpty()) {
      throw new ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "No public test cases found for this exercise"
      );
    }

    log.info(
      "Found {} public test cases for exercise {}",
      publicTestCases.size(),
      exercise.getTitle()
    );

    // 3. Chuẩn bị inputs và expected outputs (normalize \n)
    List<String> testInputs = publicTestCases
      .stream()
      .map(tc -> tc.getInput() != null ? tc.getInput().replace("\\n", "\n") : "")
      .toList();
    List<String> expectedOutputs = publicTestCases
      .stream()
      .map(tc -> tc.getOutput() != null ? tc.getOutput().replace("\\n", "\n") : "")
      .toList();

    // 4. Gửi batch lên Judge0 và đợi kết quả
    List<Map<String, Object>> rawResults = judge0Service.runBatchCode(
      request.getSourceCode(),
      request.getLanguageCode(),
      testInputs,
      expectedOutputs
    );

    log.info("rawResults: {}", rawResults);

    // 5. Decode và xử lý kết quả
    List<TestCaseResultDTO> processedResults = new ArrayList<>();
    int passedCount = 0;

    for (int i = 0; i < rawResults.size(); i++) {
      Map<String, Object> rawResult = rawResults.get(i);

      // Decode base64 outputs
      String stdout = (String) rawResult.get("stdout");
      String stderr = (String) rawResult.get("stderr");
      String compileOutput = (String) rawResult.get("compile_output");

      String decodedStdout = Base64Uitils.decodeBase64Safe(stdout);
      String decodedStderr = Base64Uitils.decodeBase64Safe(stderr);
      String decodedCompileOutput = Base64Uitils.decodeBase64Safe(compileOutput);

      // Lấy thông tin status
      Map<String, Object> statusMap = (Map<String, Object>) rawResult.get("status");
      int statusId = (int) statusMap.get("id");
      String statusDescription = (String) statusMap.get("description");

      // Kiểm tra kết quả
      boolean isPassed = statusId == 3; // Status 3 = Accepted
      if (isPassed) {
        passedCount++;
      }

      // Build status DTO
      Judge0StatusDTO status = Judge0StatusDTO.builder()
        .id(statusId)
        .description(statusDescription)
        .build();

      // Build kết quả cho từng test case (dùng input/output đã normalize)
      TestCaseResultDTO testResult = TestCaseResultDTO.builder()
        .testCaseIndex(i + 1)
        .input(testInputs.get(i))
        .expectedOutput(expectedOutputs.get(i))
        .actualOutput(decodedStdout)
        .stderr(decodedStderr)
        .compileOutput(decodedCompileOutput)
        .time((String) rawResult.get("time"))
        .memory((Integer) rawResult.get("memory"))
        .status(status)
        .passed(isPassed)
        .build();

      processedResults.add(testResult);
    }

    // 6. Build response tổng hợp
    RunCodeResponseDTO response = RunCodeResponseDTO.builder()
      .exerciseId(exercise.getId())
      .exerciseTitle(exercise.getTitle())
      .totalTestCases(publicTestCases.size())
      .passedTestCases(passedCount)
      .results(processedResults)
      .allPassed(passedCount == publicTestCases.size())
      .build();

    log.info(
      "Code execution completed: {}/{} test cases passed",
      passedCount,
      publicTestCases.size()
    );

    return response;
  }

  public List<SubmissionResultResponseDTO> getAllSubmissionResultBySubmissionId(
    String submissionId
  ) {
    return submissionResultRepository
      .findAllBySubmissionId(submissionId)
      .stream()
      .map(submissionResultMapper::toSubmissionResponseDTO)
      .toList();
  }
}
