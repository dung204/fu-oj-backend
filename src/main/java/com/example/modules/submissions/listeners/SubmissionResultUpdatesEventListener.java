package com.example.modules.submissions.listeners;

import com.example.modules.Judge0.dtos.Judge0SubmissionResponseDTO;
import com.example.modules.Judge0.services.Judge0Service;
import com.example.modules.Judge0.utils.Base64Utils;
import com.example.modules.redis.configs.listeners.RedisStreamListener;
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

  public SubmissionResultUpdatesEventListener(
    StringRedisTemplate redisTemplate,
    ObjectMapper objectMapper,
    SimpMessagingTemplate messagingTemplate,
    Judge0Service judge0Service,
    SubmissionsRepository submissionsRepository,
    TestCasesRepository testCasesRepository
  ) {
    super(redisTemplate, objectMapper);
    this.messagingTemplate = messagingTemplate;
    this.judge0Service = judge0Service;
    this.submissionsRepository = submissionsRepository;
    this.testCasesRepository = testCasesRepository;
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

    List<String> token = judge0Service.createBatchSubmissionBase64(
      sourceCode,
      languageId,
      testInputs,
      expectedOutputs
    );
    List<Judge0SubmissionResponseDTO> results = judge0Service.pollBatchResults(token);

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
    }

    messagingTemplate.convertAndSend(
      "/topic/submission-result-updates/%s".formatted(submission.getId()),
      processedResults
    );
    log.info(
      "Sent WebSocket to /topic/submission-result-updates/{} -> {}",
      submission.getId(),
      processedResults
    );
  }
}
