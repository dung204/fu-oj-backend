package com.example.modules.exams.services;

import com.example.modules.Judge0.enums.Judge0Status;
import com.example.modules.exams.dtos.ExamRankingRequestDto;
import com.example.modules.exams.dtos.ExamRankingResponseDto;
import com.example.modules.exams.entities.ExamRanking;
import com.example.modules.exams.entities.ExamSubmission;
import com.example.modules.exams.repositories.ExamRankingRepository;
import com.example.modules.exams.repositories.ExamSubmissionRepository;
import com.example.modules.exams.utils.ExamRankingSpecification;
import com.example.modules.submission_results.entities.SubmissionResult;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.submissions.repositories.SubmissionsRepository;
import com.example.modules.users.entities.User;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamRankingService {

  private final SubmissionsRepository submissionsRepository;
  private final ExamSubmissionRepository examSubmissionRepository;
  private final ExamRankingRepository examRankingRepository;

  /**
   * Scheduled task chạy mỗi 1 phút để:
   * Tìm các Submission là bài kiểm tra (isExamination = true), chưa có điểm (score = null)
   * và đã hoàn thành tất cả test cases → tính điểm và cập nhật ExamSubmission
   */
  @Scheduled(fixedRate = 60000) // 60000ms = 1 phút
  @Transactional
  public void calculateExamSubmissionsScore() {
    log.info("=== Starting scheduled check for exam submissions without score ===");

    // Tìm các submission là bài kiểm tra chưa có điểm
    List<Submission> examSubmissionsWithoutScore = submissionsRepository.findAll(
      (root, query, cb) ->
        cb.and(
          cb.isNull(root.get("score")),
          cb.isTrue(root.get("isExamination")),
          cb.isNull(root.get("deletedTimestamp"))
        )
    );

    if (examSubmissionsWithoutScore.isEmpty()) {
      log.info("No exam submissions without score found");
      return;
    }

    log.info(
      "Found {} exam submissions without score to process",
      examSubmissionsWithoutScore.size()
    );

    int processedCount = 0;
    for (Submission submission : examSubmissionsWithoutScore) {
      try {
        // Refresh submission để lấy đầy đủ submission results
        submission = submissionsRepository.findById(submission.getId()).orElse(null);

        if (submission == null) {
          continue;
        }

        // Kiểm tra xem tất cả submission results đã hoàn thành chưa
        List<SubmissionResult> results = submission.getSubmissionResults();

        if (results == null || results.isEmpty()) {
          log.debug("Submission {} has no submission results yet", submission.getId());
          continue;
        }

        boolean allCompleted = results
          .stream()
          .noneMatch(
            sr ->
              String.valueOf(Judge0Status.IN_QUEUE).equals(sr.getVerdict()) ||
              String.valueOf(Judge0Status.PROCESSING).equals(sr.getVerdict())
          );

        if (!allCompleted) {
          log.debug("Submission {} chưa hoàn thành hết test cases", submission.getId());
          continue;
        }

        // Tính passedTestCases
        long passedCount = results
          .stream()
          .filter(sr -> String.valueOf(Judge0Status.ACCEPTED).equals(sr.getVerdict()))
          .count();

        // Tính điểm dựa trên tỷ lệ passed/total
        int totalTestCases = results.size();
        double score = totalTestCases > 0 ? ((double) passedCount / totalTestCases) * 100 : 0.0;

        // Tính thời gian trung bình
        Double averageTime = results
          .stream()
          .map(SubmissionResult::getTime)
          .filter(time -> time != null && !time.isEmpty())
          .mapToDouble(Double::parseDouble)
          .average()
          .orElse(0.0);

        // Tính memory trung bình
        Double averageMemory = results
          .stream()
          .map(SubmissionResult::getMemory)
          .filter(memory -> memory != null && !memory.isEmpty())
          .mapToDouble(Double::parseDouble)
          .average()
          .orElse(0.0);

        // Cập nhật submission
        submission.setPassedTestCases((int) passedCount);
        submission.setTotalTestCases(totalTestCases);
        submission.setIsAccepted(passedCount == totalTestCases);
        submission.setTime(String.format("%.3f", averageTime));
        submission.setMemory(String.format("%.0f", averageMemory));
        submission.setScore(score);

        submissionsRepository.save(submission);

        // Cập nhật ExamSubmission
        updateExamSubmission(submission.getId(), score);

        processedCount++;
        log.info(
          "Processed exam submission {}: passed={}/{}, score={:.2f}",
          submission.getId(),
          passedCount,
          totalTestCases,
          score
        );
      } catch (Exception e) {
        log.error("Error processing exam submission {}", submission.getId(), e);
      }
    }

    log.info(
      "=== Scheduled check completed: {}/{} exam submissions processed ===",
      processedCount,
      examSubmissionsWithoutScore.size()
    );
  }

  /**
   * Cập nhật điểm cho ExamSubmission dựa trên submissionId
   */
  private void updateExamSubmission(String submissionId, Double score) {
    try {
      // Tìm ExamSubmission theo submissionId
      List<ExamSubmission> examSubmissions = examSubmissionRepository.findAll((root, query, cb) ->
        cb.equal(root.get("submissionId"), submissionId)
      );

      if (examSubmissions.isEmpty()) {
        log.warn("No ExamSubmission found for submissionId: {}", submissionId);
        return;
      }

      for (ExamSubmission examSubmission : examSubmissions) {
        examSubmission.setScore(score);
        examSubmissionRepository.save(examSubmission);
        log.info(
          "Updated ExamSubmission {} with score {} for submissionId {}",
          examSubmission.getId(),
          score,
          submissionId
        );
      }
    } catch (Exception e) {
      log.error("Error updating ExamSubmission for submissionId {}", submissionId, e);
    }
  }

  public List<ExamRankingResponseDto> getExamRankings(ExamRankingRequestDto dto, User currentUser) {
    String effectiveUserId = dto.getUserId();
    if (currentUser != null && currentUser.getAccount() != null) {
      // Nếu là STUDENT thì chỉ được xem của chính mình
      var role = currentUser.getAccount().getRole();
      if (role != null && role.name().equals("STUDENT")) {
        effectiveUserId = currentUser.getId();
      }
    }

    var spec = ExamRankingSpecification.builder()
      .withExamId(dto.getExamId())
      .withUserId(effectiveUserId)
      .withTotalScore(dto.getTotalScore())
      .withMinScore(dto.getMinScore())
      .withMaxScore(dto.getMaxScore())
      .notDeleted()
      .build();

    List<ExamRanking> rankings = examRankingRepository.findAll(spec);

    return rankings
      .stream()
      .map(er ->
        ExamRankingResponseDto.builder()
          .exam(er.getExam())
          .user(er.getUser())
          .totalScore(er.getTotalScore())
          .build()
      )
      .collect(Collectors.toList());
  }
}
