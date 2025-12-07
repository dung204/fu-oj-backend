package com.example.modules.submissions.listeners;

import com.example.modules.Judge0.dtos.Judge0SubmissionResponseDTO;
import com.example.modules.Judge0.services.Judge0Service;
import com.example.modules.Judge0.utils.Base64Utils;
import com.example.modules.redis.configs.listeners.RedisStreamListener;
import com.example.modules.submission_results.entities.SubmissionResult;
import com.example.modules.submission_results.repositories.SubmissionResultRepository;
import com.example.modules.submission_results.services.SubmissionResultsService;
import com.example.modules.submissions.dtos.SubmissionResultUpdateEventDTO;
import com.example.modules.submissions.dtos.TestCaseResultDTO;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.submissions.enums.Verdict;
import com.example.modules.submissions.repositories.SubmissionsRepository;
import com.example.modules.test_cases.entities.TestCase;
import com.example.modules.test_cases.repositories.TestCasesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SubmissionResultUpdatesEventListener
  extends RedisStreamListener<SubmissionResultUpdateEventDTO> {

  private final SimpMessagingTemplate messagingTemplate;
  private final Judge0Service judge0Service;
  private final SubmissionsRepository submissionsRepository;
  private final TestCasesRepository testCasesRepository;
  private final SubmissionResultRepository submissionResultRepository;
  private final SubmissionResultsService submissionResultsService;

  public SubmissionResultUpdatesEventListener(
    StringRedisTemplate redisTemplate,
    ObjectMapper objectMapper,
    SimpMessagingTemplate messagingTemplate,
    Judge0Service judge0Service,
    SubmissionsRepository submissionsRepository,
    TestCasesRepository testCasesRepository,
    SubmissionResultRepository submissionResultRepository,
    SubmissionResultsService submissionResultsService
  ) {
    super(redisTemplate, objectMapper);
    this.messagingTemplate = messagingTemplate;
    this.judge0Service = judge0Service;
    this.submissionsRepository = submissionsRepository;
    this.testCasesRepository = testCasesRepository;
    this.submissionResultRepository = submissionResultRepository;
    this.submissionResultsService = submissionResultsService;
  }

  @Override
  public String getStreamKey() {
    return "submissions:events:submission-result-updates";
  }

  @Override
  public String getConsumerGroup() {
    return "submission-result-updates-workers";
  }

  @Override
  public Class<SubmissionResultUpdateEventDTO> getTargetType() {
    return SubmissionResultUpdateEventDTO.class;
  }

  @Override
  protected void process(String messageId, SubmissionResultUpdateEventDTO dto) {
    String submissionId = dto.getSubmissionId();
    String sourceCode = dto.getSourceCode();
    String languageId = dto.getLanguageCode();
    List<String> testInputs = dto.getTestInputs();
    List<String> expectedOutputs = dto.getExpectedOutputs();

    Submission submission = submissionsRepository.findById(submissionId).get();

    List<TestCase> testCases = testCasesRepository.findAllByExerciseId(
      submission.getExercise().getId()
    );

    List<String> tokens = judge0Service.createBatchSubmissionBase64(
      sourceCode,
      languageId,
      testInputs,
      expectedOutputs
    );
    List<Judge0SubmissionResponseDTO> results = judge0Service.pollBatchResults(tokens);

    log.info("Received {} results from Judge0", results.size());

    List<TestCaseResultDTO> processedResults = new ArrayList<>();

    for (int i = 0; i < results.size(); i++) {
      Judge0SubmissionResponseDTO result = results.get(i);

      // Decode base64 outputs
      String decodedStdout = Base64Utils.decodeBase64Safe(result.getStdout());
      String decodedStderr = Base64Utils.decodeBase64Safe(result.getStderr());
      String decodedCompileOutput = Base64Utils.decodeBase64Safe(result.getCompileOutput());

      // Kiểm tra kết quả
      Verdict verdict = Verdict.getVerdictFromJudge0Response(result);

      // Build kết quả cho từng test case (dùng input/output đã normalize)
      TestCaseResultDTO testResult = TestCaseResultDTO.builder()
        .testCaseId(testCases.get(i).getId())
        .testCaseIndex(i + 1)
        .input(testInputs.get(i))
        .expectedOutput(expectedOutputs.get(i))
        .actualOutput(decodedStdout)
        .stderr(decodedStderr)
        .compileOutput(decodedCompileOutput)
        .time(result.getTime())
        .memory(result.getMemory())
        .verdict(verdict)
        .passed(verdict == Verdict.ACCEPTED)
        .isPublic(testCases.get(i).getIsPublic())
        .build();

      processedResults.add(testResult);

      submissionResultRepository.save(
        SubmissionResult.builder()
          .submission(submission)
          .testCase(testCases.get(i))
          .token(tokens.get(i))
          .actualOutput(decodedStdout)
          .stderr(decodedStderr)
          .verdict(verdict.getValue())
          .time(result.getTime())
          .memory(result.getMemory() != null ? result.getMemory().toString() : null)
          .build()
      );
    }

    // Chỉ tính điểm MỘT LẦN sau khi hoàn thành TẤT CẢ test cases
    submissionResultsService.updateSubmissionScore(submission);

    messagingTemplate.convertAndSend(
      "/topic/submission-result-updates/%s".formatted(submission.getId()),
      processedResults
    );

    // sau khi bắn socket xong thì redis mới acknowledge message ( bỏ message ra queue )
    log.info(
      "Sent WebSocket to /topic/submission-result-updates/{} -> {}",
      submission.getId(),
      processedResults
    );
  }
}
