package com.example.modules.submission_results.services;

import com.example.modules.Judge0.dtos.Judge0SubmissionResponseDTO;
import com.example.modules.Judge0.services.Judge0Service;
import com.example.modules.Judge0.utils.Base64Utils;
import com.example.modules.scores.services.ScoresService;
import com.example.modules.submission_results.entities.SubmissionResult;
import com.example.modules.submission_results.repositories.SubmissionResultRepository;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.submissions.enums.Verdict;
import com.example.modules.submissions.repositories.SubmissionsRepository;
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
  private final SubmissionsRepository submissionsRepository;
  private final ScoresService scoresService;

  /**
   * Scheduled task chạy mỗi 1 phút để:
   * 1. Kiểm tra và cập nhật các SubmissionResult đang pending (IN_QUEUE/PROCESSING)
   * 2. Tìm các Submission chưa có điểm và đã hoàn thành → tính điểm
   */
  @Scheduled(fixedRate = 60000) // 60000ms = 1 phút
  @Transactional
  public void checkPendingSubmissions() {
    log.info("=== Starting scheduled check for pending submissions ===");

    // PART 1: Cập nhật các submission result đang pending
    updatePendingSubmissionResults();

    // PART 2: Tính điểm cho các submission đã hoàn thành nhưng chưa có điểm
    updateSubmissionsWithoutScore();

    log.info("=== Scheduled check completed ===");
  }

  /**
   * Part 1: Cập nhật các SubmissionResult có verdict IN_QUEUE hoặc PROCESSING
   */
  private void updatePendingSubmissionResults() {
    List<SubmissionResult> pendingResults = submissionResultRepository.findByVerdictIn(
      List.of(Verdict.IN_QUEUE.getValue(), Verdict.PROCESSING.getValue())
    );

    if (pendingResults.isEmpty()) {
      log.info("No pending submission results found");
      return;
    }

    log.info("Found {} pending submission results to check", pendingResults.size());

    int updatedCount = 0;
    for (SubmissionResult sr : pendingResults) {
      try {
        // Gọi Judge0 để lấy kết quả
        Judge0SubmissionResponseDTO response = judge0Service.getSubmission(sr.getToken());

        log.info("Submission time: {}", response.getTime());

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

        // Decode outputs
        String decodedStdout = Base64Utils.decodeBase64Safe(response.getStdout());
        String decodedStderr = Base64Utils.decodeBase64Safe(response.getStderr());

        // Determine verdict
        Verdict verdict = Verdict.getVerdictFromJudge0Response(response);

        // Update submission result
        sr.setVerdict(verdict.getValue());
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
          verdict.getValue()
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
      "Part 1 completed: {}/{} submission results updated",
      updatedCount,
      pendingResults.size()
    );
  }

  /**
   * Part 2: Tìm các Submission chưa có điểm (score is null)
   * và đã hoàn thành tất cả test cases → tính điểm
   */
  private void updateSubmissionsWithoutScore() {
    // Tìm các submission chưa có điểm (score is null)
    List<Submission> submissionsWithoutScore = submissionsRepository.findAll((root, query, cb) ->
      cb.and(cb.isNull(root.get("score")), cb.isNull(root.get("deletedTimestamp")))
    );

    if (submissionsWithoutScore.isEmpty()) {
      log.info("No submissions without score found");
      return;
    }

    log.info("Found {} submissions without score to check", submissionsWithoutScore.size());

    updateSubmissionsScore(submissionsWithoutScore);
  }

  /**
   * Cập nhật điểm cho danh sách submission
   * Tính lại passedTestCases, totalTestCases, isAccepted, score
   * Sau đó cập nhật tổng điểm user
   */
  private void updateSubmissionsScore(List<Submission> submissions) {
    for (Submission submission : submissions) {
      try {
        // Refresh submission với tất cả submission results
        submission = submissionsRepository.findById(submission.getId()).orElse(null);

        if (submission == null) {
          continue;
        }

        // Kiểm tra xem tất cả submission results đã hoàn thành chưa
        List<SubmissionResult> results = submission.getSubmissionResults();
        boolean allCompleted = results
          .stream()
          .noneMatch(
            sr ->
              Verdict.IN_QUEUE.getValue().equals(sr.getVerdict()) ||
              Verdict.PROCESSING.getValue().equals(sr.getVerdict())
          );

        if (!allCompleted) {
          log.debug("Submission {} chưa hoàn thành hết test cases", submission.getId());
          continue;
        }

        // Tính passedTestCases
        long passedCount = results
          .stream()
          .filter(sr -> Verdict.ACCEPTED.getValue().equals(sr.getVerdict()))
          .count();

        // calculate time average base on time of all submission results
        Double averageTime = results
          .stream()
          .map(SubmissionResult::getTime)
          .filter(time -> time != null)
          .mapToDouble(time -> Double.parseDouble(time))
          .average()
          .orElse(0.0);

        // caculate average memory of all submission results
        Double averageMemory = results
          .stream()
          .map(SubmissionResult::getMemory)
          .filter(memory -> memory != null)
          .mapToDouble(time -> Double.parseDouble(time))
          .average()
          .orElse(0.0);

        // Cập nhật submission
        submission.setPassedTestCases((int) passedCount);
        submission.setTotalTestCases(results.size());
        submission.setIsAccepted(passedCount == results.size());
        submission.setTime(String.valueOf(averageTime));
        submission.setMemory(String.valueOf(averageMemory));

        // Tính điểm
        double score = scoresService.calculateSubmissionScore(submission);
        submission.setScore(score);

        submissionsRepository.save(submission);

        log.info(
          "Updated submission {}: passed={}/{}, isAccepted={}, score={}",
          submission.getId(),
          passedCount,
          results.size(),
          submission.getIsAccepted(),
          score
        );

        // Cập nhật điểm user (cộng dần)
        scoresService.updateUserScoreBySubmission(submission);
      } catch (Exception e) {
        log.error("Error updating score for submission {}", submission.getId(), e);
      }
    }
  }
}
