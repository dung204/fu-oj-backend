package com.example.modules.submission_results.services;

import com.example.modules.Judge0.dtos.Judge0SubmissionResponseDTO;
import com.example.modules.Judge0.enums.Judge0Status;
import com.example.modules.Judge0.services.Judge0Service;
import com.example.modules.Judge0.uitils.Base64Uitils;
import com.example.modules.submission_results.entities.SubmissionResult;
import com.example.modules.submission_results.repositories.SubmissionResultRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionResultsService {

  private final SubmissionResultRepository submissionResultRepository;
  private final Judge0Service judge0Service;

  /**
   * Scheduled task chạy mỗi 1 phút để kiểm tra và cập nhật các submission đang pending
   * Tìm các SubmissionResult có verdict = IN_QUEUE hoặc PROCESSING
   * Gọi Judge0 để lấy kết quả và cập nhật vào DB
   */
  @Scheduled(fixedRate = 60000) // 60000ms = 1 phút
  @Transactional
  public void checkPendingSubmissions() {
    log.info("=== Starting scheduled check for pending submissions ===");

    // 1. Tìm tất cả submission có verdict IN_QUEUE hoặc PROCESSING
    List<SubmissionResult> pendingResults = submissionResultRepository.findByVerdictIn(
      List.of(String.valueOf(Judge0Status.IN_QUEUE), String.valueOf(Judge0Status.PROCESSING))
    );

    if (pendingResults.isEmpty()) {
      log.info("No pending submissions found");
      return;
    }

    log.info("Found {} pending submissions to check", pendingResults.size());

    // 2. Kiểm tra từng submission
    int updatedCount = 0;
    for (SubmissionResult sr : pendingResults) {
      try {
        // Gọi Judge0 để lấy kết quả
        Judge0SubmissionResponseDTO response = judge0Service.getSubmission(sr.getToken());

        if (response == null || response.getStatus() == null) {
          log.warn("No result returned for token: {}", sr.getToken());
          continue;
        }

        // Parse status
        int statusId = response.getStatus().getId();
        String statusDesc = response.getStatus().getDescription();

        // Nếu vẫn đang IN_QUEUE hoặc PROCESSING thì skip
        if (statusId == 1 || statusId == 2) {
          log.debug("Token {} still processing (status: {})", sr.getToken(), statusDesc);
          continue;
        }

        // 3. Decode outputs
        String decodedStdout = Base64Uitils.decodeBase64Safe(response.getStdout());
        String decodedStderr = Base64Uitils.decodeBase64Safe(response.getStderr());

        // 4. Determine verdict
        String expected = sr.getTestCase().getOutput() != null
          ? sr.getTestCase().getOutput().trim()
          : "";
        String actual = decodedStdout != null ? decodedStdout.trim() : "";

        String verdict;
        switch (statusId) {
          case 3 -> verdict = expected.equals(actual)
            ? String.valueOf(Judge0Status.ACCEPTED)
            : String.valueOf(Judge0Status.WRONG_ANSWER);
          case 4 -> verdict = String.valueOf(Judge0Status.WRONG_ANSWER);
          case 5 -> verdict = String.valueOf(Judge0Status.TIME_LIMIT_EXCEEDED);
          case 6 -> verdict = String.valueOf(Judge0Status.COMPILATION_ERROR);
          case 7, 8, 9, 10, 11, 12 -> verdict = String.valueOf(Judge0Status.RUNTIME_ERROR);
          case 13 -> verdict = String.valueOf(Judge0Status.INTERNAL_ERROR);
          case 14 -> verdict = String.valueOf(Judge0Status.EXEC_FORMAT_ERROR);
          default -> verdict = expected.equals(actual)
            ? String.valueOf(Judge0Status.ACCEPTED)
            : String.valueOf(Judge0Status.WRONG_ANSWER);
        }

        // 5. Update submission result
        sr.setVerdict(verdict);
        sr.setActualOutput(decodedStdout);
        sr.setStderr(decodedStderr);
        sr.setTime(response.getTime());
        sr.setMemory(response.getMemory() != null ? String.valueOf(response.getMemory()) : null);

        submissionResultRepository.save(sr);
        updatedCount++;

        log.info(
          "Updated submission result {} - Token: {}, Verdict: {}",
          sr.getId(),
          sr.getToken(),
          verdict
        );
      } catch (Exception e) {
        log.error(
          "Error checking submission result {} with token {}",
          sr.getId(),
          sr.getToken(),
          e
        );
      }
    }

    log.info(
      "=== Scheduled check completed: {}/{} submissions updated ===",
      updatedCount,
      pendingResults.size()
    );
  }
}
