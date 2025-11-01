package com.example.modules.submissions.services;

import com.example.modules.Judge0.dtos.Judge0CallbackRequestDTO;
import com.example.modules.Judge0.dtos.Judge0SubmissionResponseDTO;
import com.example.modules.Judge0.services.Judge0Service;
import com.example.modules.Judge0.utils.Base64Utils;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.redis.configs.publishers.NewSubmissionsPublisher;
import com.example.modules.redis.configs.publishers.SubmissionResultUpdatesPublisher;
import com.example.modules.submission_results.dtos.SubmissionResultResponseDTO;
import com.example.modules.submission_results.entities.SubmissionResult;
import com.example.modules.submission_results.repositories.SubmissionResultRepository;
import com.example.modules.submissions.dtos.RunCodeRequest;
import com.example.modules.submissions.dtos.RunCodeResponseDTO;
import com.example.modules.submissions.dtos.SubmissionRequest;
import com.example.modules.submissions.dtos.SubmissionResponseDTO;
import com.example.modules.submissions.dtos.SubmissionStatisticsRequestDTO;
import com.example.modules.submissions.dtos.SubmissionStatisticsResponseDTO;
import com.example.modules.submissions.dtos.SubmissionsSearchDTO;
import com.example.modules.submissions.dtos.TestCaseResultDTO;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.submissions.enums.Verdict;
import com.example.modules.submissions.repositories.SubmissionsRepository;
import com.example.modules.submissions.utils.SubmissionMapper;
import com.example.modules.submissions.utils.SubmissionResultMapper;
import com.example.modules.submissions.utils.SubmissionsSpecification;
import com.example.modules.test_cases.entities.TestCase;
import com.example.modules.test_cases.repositories.TestCasesRepository;
import com.example.modules.users.entities.User;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubmissionsService {

  Judge0Service judge0Service;
  SubmissionsRepository submissionsRepository;
  TestCasesRepository testCaseRepository;
  SubmissionResultRepository submissionResultRepository;
  ExercisesRepository exerciseRepository;
  SubmissionResultMapper submissionResultMapper;
  SubmissionLimitService submissionLimitService;
  SubmissionMapper submissionMapper;
  SubmissionResultUpdatesPublisher submissionResultUpdatesPublisher;
  NewSubmissionsPublisher newSubmissionsPublisher;

  public Page<SubmissionResponseDTO> getAllSubmissions(SubmissionsSearchDTO submissionsSearchDTO) {
    return submissionsRepository
      .findAll(
        SubmissionsSpecification.builder()
          .withStudentId(submissionsSearchDTO.getStudent())
          .withExerciseId(submissionsSearchDTO.getExercise())
          .isOneOfStatuses(submissionsSearchDTO.getStatus())
          .isOneOfLanguageCodes(submissionsSearchDTO.getLanguageCode())
          .notDeleted()
          .build(),
        submissionsSearchDTO.toPageRequest()
      )
      .map(submissionMapper::toSubmissionResponseDTO);
  }

  public SubmissionResponseDTO createSubmissionBase64(SubmissionRequest request, User currentUser) {
    Exercise exercise = exerciseRepository
      .findById(String.valueOf(request.getExerciseId()))
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercise not found"));

    if (exercise.getMaxSubmissions() != 0) {
      log.info(
        "Checking submission limit for user {} on exercise {}",
        currentUser.getId(),
        exercise.getId()
      );
      submissionLimitService.checkAndIncrease(currentUser.getId(), exercise.getId());
    }

    // get all test cases of exercise
    List<TestCase> testCases = testCaseRepository.findAllByExerciseId((exercise.getId()));

    // create submission
    Submission submission = Submission.builder()
      .user(currentUser)
      .exercise(exercise)
      .sourceCode(request.getSourceCode())
      .languageCode(request.getLanguageCode())
      .time(null)
      .memory(null)
      .passedTestCases(0)
      .totalTestCases(testCases.size())
      .isAccepted(false)
      .score(null)
      .build();
    submission = submissionsRepository.save(submission);

    // bàn lại format lưu test case với ae sau
    List<String> testInputs = testCases.stream().map(TestCase::getInput).toList();

    List<String> expectedOutputs = testCases.stream().map(TestCase::getOutput).toList();

    // Gửi batch lên Judge0 -> nhận list token
    List<String> tokens = judge0Service.createBatchSubmissionBase64(
      request.getSourceCode(),
      request.getLanguageCode(),
      testInputs,
      expectedOutputs
    );

    // Gắn từng token với từng test case -> lưu SubmissionResult với verdict = IN_QUEUE
    for (int i = 0; i < testCases.size(); i++) {
      SubmissionResult result = SubmissionResult.builder()
        .submission(submission)
        .testCase(testCases.get(i))
        .token(tokens.get(i))
        .verdict(Verdict.IN_QUEUE.getValue())
        .build();

      log.info("Submission {}, token: {}, verdict: {}", i, tokens.get(i), Judge0Status.IN_QUEUE);
      submissionResultRepository.save(result);
    }

    log.info("Submission {} created with {} test cases", submission.getId(), testCases.size());
    SubmissionResponseDTO responseDTO = submissionMapper.toSubmissionResponseDTO(submission);
    newSubmissionsPublisher.publishNewSubmission(responseDTO);
    return responseDTO;
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
  public void handleCallback(Judge0CallbackRequestDTO callback) {
    log.info("Received callback result: {}", callback);

    //1. Extract basic fields
    String token = callback.getToken();

    //2. Decode base64 safely using Base64Utils
    String decodedStdout = Base64Utils.decodeBase64Safe(callback.getStdout());
    String decodedStderr = Base64Utils.decodeBase64Safe(callback.getStderr());
    String decodedCompileOutput = Base64Utils.decodeBase64Safe(callback.getCompileOutput());

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
    Judge0SubmissionResponseDTO judge0SubmissionResponse = judge0Service.getSubmission(token);
    Verdict verdict = Verdict.getVerdictFromJudge0Response(judge0SubmissionResponse);

    //5. Update submission result
    sr.setVerdict(verdict.getValue());
    sr.setActualOutput(decodedStdout);
    sr.setStderr(decodedStderr);
    submissionResultRepository.save(sr);

    boolean isPublic = sr.getTestCase().getIsPublic();

    //6. Publish Redis message -> send WebSocket to FE (via SubmissionSubscriber)
    TestCaseResultDTO testCaseResult = TestCaseResultDTO.builder()
      .submissionId(sr.getSubmission().getId())
      .token(sr.getToken())
      .userId(sr.getSubmission().getUser().getId())
      .testCaseId(sr.getTestCase().getId())
      .input(isPublic ? sr.getTestCase().getInput() : null)
      .expectedOutput(isPublic ? expected : null)
      .actualOutput(isPublic ? actual : null)
      .stderr(isPublic ? decodedStderr : null)
      .compileOutput(decodedCompileOutput)
      .time(callback.getTime())
      .memory(callback.getMemory())
      .verdict(verdict)
      .passed(verdict == Verdict.ACCEPTED)
      .isPublic(isPublic)
      .build();

    submissionResultUpdatesPublisher.publishSubmissionResultUpdate(testCaseResult);

    log.info("Callback for token {} => {}", token, verdict);
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
    List<Judge0SubmissionResponseDTO> results = judge0Service.runBatchCode(
      request.getSourceCode(),
      request.getLanguageCode(),
      testInputs,
      expectedOutputs
    );

    log.info("Received {} results from Judge0", results.size());

    // 5. Decode và xử lý kết quả
    List<TestCaseResultDTO> processedResults = new ArrayList<>();
    int passedCount = 0;

    for (int i = 0; i < results.size(); i++) {
      Judge0SubmissionResponseDTO result = results.get(i);

      // Decode base64 outputs
      String decodedStdout = Base64Utils.decodeBase64Safe(result.getStdout());
      String decodedStderr = Base64Utils.decodeBase64Safe(result.getStderr());
      String decodedCompileOutput = Base64Utils.decodeBase64Safe(result.getCompileOutput());

      // Kiểm tra kết quả
      Verdict verdict = Verdict.getVerdictFromJudge0Response(result);

      boolean isPassed = verdict == Verdict.ACCEPTED;
      if (isPassed) {
        passedCount++;
      }

      // Build kết quả cho từng test case (dùng input/output đã normalize)
      TestCaseResultDTO testResult = TestCaseResultDTO.builder()
        .testCaseId(publicTestCases.get(i).getId())
        .testCaseIndex(i + 1)
        .input(testInputs.get(i))
        .expectedOutput(expectedOutputs.get(i))
        .actualOutput(decodedStdout)
        .stderr(decodedStderr)
        .compileOutput(decodedCompileOutput)
        .time(result.getTime())
        .memory(result.getMemory())
        .verdict(verdict)
        .passed(isPassed)
        .isPublic(true)
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
      .map(submissionResultMapper::toSubmissionResultResponseDTO)
      .toList();
  }

  public SubmissionStatisticsResponseDTO getSubmissionStatistics(
    SubmissionStatisticsRequestDTO requestDTO
  ) {
    return SubmissionStatisticsResponseDTO.builder()
      .accepted(
        requestDTO.getStatus() != null &&
            !requestDTO.getStatus().contains(Verdict.ACCEPTED.getValue())
          ? 0
          : submissionsRepository.count(
            SubmissionsSpecification.builder()
              .withExerciseId(requestDTO.getExercise())
              .withStudentId(requestDTO.getStudent())
              .isOneOfLanguageCodes(requestDTO.getLanguageCode())
              .isOneOfStatuses(List.of(Verdict.ACCEPTED.getValue()))
              .build()
          )
      )
      .wrongAnswer(
        requestDTO.getStatus() != null &&
            !requestDTO.getStatus().contains(Verdict.WRONG_ANSWER.getValue())
          ? 0
          : submissionsRepository.count(
            SubmissionsSpecification.builder()
              .withExerciseId(requestDTO.getExercise())
              .withStudentId(requestDTO.getStudent())
              .isOneOfLanguageCodes(requestDTO.getLanguageCode())
              .isOneOfStatuses(List.of(Verdict.WRONG_ANSWER.getValue()))
              .build()
          )
      )
      .timeLimitExceeded(
        requestDTO.getStatus() != null &&
            !requestDTO.getStatus().contains(Verdict.TIME_LIMIT_EXCEEDED.getValue())
          ? 0
          : submissionsRepository.count(
            SubmissionsSpecification.builder()
              .withExerciseId(requestDTO.getExercise())
              .withStudentId(requestDTO.getStudent())
              .isOneOfLanguageCodes(requestDTO.getLanguageCode())
              .isOneOfStatuses(List.of(Verdict.TIME_LIMIT_EXCEEDED.getValue()))
              .build()
          )
      )
      .compilationError(
        requestDTO.getStatus() != null &&
            !requestDTO.getStatus().contains(Verdict.COMPILATION_ERROR.getValue())
          ? 0
          : submissionsRepository.count(
            SubmissionsSpecification.builder()
              .withExerciseId(requestDTO.getExercise())
              .withStudentId(requestDTO.getStudent())
              .isOneOfLanguageCodes(requestDTO.getLanguageCode())
              .isOneOfStatuses(List.of(Verdict.COMPILATION_ERROR.getValue()))
              .build()
          )
      )
      .runtimeError(
        requestDTO.getStatus() != null &&
            !requestDTO.getStatus().contains(Verdict.RUNTIME_ERROR.getValue())
          ? 0
          : submissionsRepository.count(
            SubmissionsSpecification.builder()
              .withExerciseId(requestDTO.getExercise())
              .withStudentId(requestDTO.getStudent())
              .isOneOfLanguageCodes(requestDTO.getLanguageCode())
              .isOneOfStatuses(List.of(Verdict.RUNTIME_ERROR.getValue()))
              .build()
          )
      )
      .memoryLimitExceeded(
        requestDTO.getStatus() != null &&
            !requestDTO.getStatus().contains(Verdict.MEMORY_LIMIT_EXCEEDED.getValue())
          ? 0
          : submissionsRepository.count(
            SubmissionsSpecification.builder()
              .withExerciseId(requestDTO.getExercise())
              .withStudentId(requestDTO.getStudent())
              .isOneOfLanguageCodes(requestDTO.getLanguageCode())
              .isOneOfStatuses(List.of(Verdict.MEMORY_LIMIT_EXCEEDED.getValue()))
              .build()
          )
      )
      .totalCount(
        submissionsRepository.count(
          SubmissionsSpecification.builder()
            .withExerciseId(requestDTO.getExercise())
            .withStudentId(requestDTO.getStudent())
            .isOneOfLanguageCodes(requestDTO.getLanguageCode())
            .isOneOfStatuses(requestDTO.getStatus())
            .build()
        )
      )
      .build();
  }
}
