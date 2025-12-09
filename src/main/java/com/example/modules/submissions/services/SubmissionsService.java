package com.example.modules.submissions.services;

import com.example.modules.Judge0.dtos.Judge0SubmissionResponseDTO;
import com.example.modules.Judge0.services.Judge0Service;
import com.example.modules.Judge0.utils.Base64Utils;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.redis.publishers.RedisStreamPublisher;
import com.example.modules.submission_results.entities.SubmissionResult;
import com.example.modules.submission_results.repositories.SubmissionResultRepository;
import com.example.modules.submissions.dtos.RunCodeRequest;
import com.example.modules.submissions.dtos.RunCodeResponseDTO;
import com.example.modules.submissions.dtos.SubmissionRequest;
import com.example.modules.submissions.dtos.SubmissionResponseDTO;
import com.example.modules.submissions.dtos.SubmissionResultUpdateEventDTO;
import com.example.modules.submissions.dtos.SubmissionStatisticsRequestDTO;
import com.example.modules.submissions.dtos.SubmissionStatisticsResponseDTO;
import com.example.modules.submissions.dtos.SubmissionsSearchDTO;
import com.example.modules.submissions.dtos.TestCaseResultDTO;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.submissions.enums.Verdict;
import com.example.modules.submissions.exceptions.SubmissionNotFound;
import com.example.modules.submissions.repositories.SubmissionsRepository;
import com.example.modules.submissions.utils.SubmissionMapper;
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
  SubmissionLimitService submissionLimitService;
  SubmissionMapper submissionMapper;
  RedisStreamPublisher redisStreamPublisher;

  public Page<SubmissionResponseDTO> getAllSubmissions(SubmissionsSearchDTO submissionsSearchDTO) {
    return submissionsRepository
      .findAll(
        SubmissionsSpecification.builder()
          .withStudentId(submissionsSearchDTO.getStudent())
          .withExerciseId(submissionsSearchDTO.getExercise())
          .withExamination(submissionsSearchDTO.getIsExamination())
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
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài tập")
      );

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
    boolean isExamination = request.isExamination();

    // create submission
    Submission submission = submissionsRepository.save(
      Submission.builder()
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
        .isExamination(isExamination)
        .build()
    );

    List<String> testInputs = testCases.stream().map(TestCase::getInput).toList();

    List<String> expectedOutputs = testCases.stream().map(TestCase::getOutput).toList();

    redisStreamPublisher.send(
      "submissions:events:submission-result-updates",
      new SubmissionResultUpdateEventDTO(
        submission.getId(),
        submission.getSourceCode(),
        submission.getLanguageCode(),
        testInputs,
        expectedOutputs
      )
    );

    log.info("Submission {} created with {} test cases", submission.getId(), testCases.size());
    SubmissionResponseDTO responseDTO = submissionMapper.toSubmissionResponseDTO(submission);
    return responseDTO;
  }

  public RunCodeResponseDTO runCode(RunCodeRequest request) {
    log.info("Running code for exercise: {}", request.getExerciseId());

    // 1. Lấy exercise từ DB
    Exercise exercise = exerciseRepository
      .findById(request.getExerciseId())
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy bài tập")
      );

    // 2. Lấy tất cả test cases public
    List<TestCase> publicTestCases = testCaseRepository.findAllByExerciseIdAndIsPublicTrue(
      exercise.getId()
    );

    if (publicTestCases.isEmpty()) {
      throw new ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "Không tìm thấy test case công khai cho bài tập này"
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

  public SubmissionResponseDTO getAllSubmissionResultBySubmissionId(String submissionId) {
    Submission submission = submissionsRepository
      .findById(submissionId)
      .orElseThrow(() -> new SubmissionNotFound("Không tìm thấy bài nộp với ID: " + submissionId));

    // get submission results
    List<SubmissionResult> submissionResults = submissionResultRepository.findAllBySubmissionId(
      submissionId
    );

    // attach results to submission entity for mapper usage
    submission.setSubmissionResults(submissionResults);

    // map to response dto (SubmissionMapper handles nested mappings & derived
    // fields)
    return submissionMapper.toSubmissionResponseDTO(submission);
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
