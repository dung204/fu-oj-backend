package com.example.modules.exams.services;

import com.example.modules.Judge0.enums.Judge0Status;
import com.example.modules.exams.dtos.ExamRankingRequestDto;
import com.example.modules.exams.dtos.ExamRankingResponseDto;
import com.example.modules.exams.entities.ExamRanking;
import com.example.modules.exams.entities.ExamSubmission;
import com.example.modules.exams.repositories.ExamRankingRepository;
import com.example.modules.exams.repositories.ExamSubmissionRepository;
import com.example.modules.exams.utils.ExamRankingMapper;
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
  private final ExamRankingMapper examRankingMapper;

  /**
   * Scheduled task chạy mỗi 1 phút để:
   * Tìm các Submission là bài kiểm tra (isExamination = true), chưa có điểm (score = null)
   * và đã hoàn thành tất cả test cases → tính điểm và cập nhật ExamSubmission
   */
  @Scheduled(fixedRate = 60000) // 60000ms = 1 phút
  @Transactional
  public void calculateExamSubmissionsScore() {
    log.info("=== Starting scheduled check for ExamSubmission without score ===");

    // Tìm các ExamSubmission chưa có điểm
    List<ExamSubmission> examSubmissionsWithoutScore = examSubmissionRepository.findAll(
      (root, query, cb) ->
        cb.and(cb.isNull(root.get("score")), cb.isNull(root.get("deletedTimestamp")))
    );

    if (examSubmissionsWithoutScore.isEmpty()) {
      log.info("No ExamSubmission without score found");
      return;
    }

    log.info(
      "Found {} ExamSubmission without score to process",
      examSubmissionsWithoutScore.size()
    );

    int processedCount = 0;
    for (ExamSubmission examSubmission : examSubmissionsWithoutScore) {
      try {
        // Lấy Submission gốc theo submissionId
        Submission submission = submissionsRepository
          .findById(examSubmission.getSubmissionId())
          .orElse(null);

        if (submission == null) {
          log.warn(
            "Linked Submission not found for submissionId {}",
            examSubmission.getSubmissionId()
          );
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

        // Cập nhật điểm cho ExamSubmission (không cập nhật Submission)
        examSubmission.setScore(score);
        examSubmissionRepository.save(examSubmission);

        // Upsert ExamRanking theo (exam, user)
        upsertExamRanking(examSubmission);

        processedCount++;
        log.info("Processed ExamSubmission {}: score={}", examSubmission.getId(), score);
      } catch (Exception e) {
        log.error("Error processing ExamSubmission {}", examSubmission.getId(), e);
      }
    }

    log.info("=== Scheduled check completed: {} ExamSubmission processed ===", processedCount);
  }

  /**
   * Cập nhật điểm cho ExamSubmission dựa trên submissionId
   */
  private void upsertExamRanking(ExamSubmission updatedExamSubmission) {
    try {
      String examId = updatedExamSubmission.getExam().getId();
      String userId = updatedExamSubmission.getUser().getId();

      // Tính tổng điểm từ tất cả ExamSubmission của (exam, user)
      List<ExamSubmission> userExamSubmissions = examSubmissionRepository.findByExamIdAndUserId(
        examId,
        userId
      );
      double totalScore = userExamSubmissions
        .stream()
        .map(ExamSubmission::getScore)
        .filter(s -> s != null)
        .mapToDouble(Double::doubleValue)
        .sum();

      // Mỗi bài chỉ nộp 1 lần -> tính trực tiếp theo danh sách ExamSubmission
      double numberOfExercises = (double) userExamSubmissions.size();
      double numberOfCompletedExercises = (double) userExamSubmissions
        .stream()
        .map(ExamSubmission::getScore)
        .filter(s -> s != null && s >= 100.0)
        .count();

      // Tìm hoặc tạo ExamRanking
      List<ExamRanking> existing = examRankingRepository.findAll((root, query, cb) ->
        cb.and(
          cb.equal(root.get("exam").get("id"), examId),
          cb.equal(root.get("user").get("id"), userId)
        )
      );

      ExamRanking ranking;
      if (existing.isEmpty()) {
        ranking = ExamRanking.builder()
          .exam(updatedExamSubmission.getExam())
          .user(updatedExamSubmission.getUser())
          .totalScore(totalScore)
          .numberOfExercises(numberOfExercises)
          .numberOfCompletedExercises(numberOfCompletedExercises)
          .build();
      } else {
        ranking = existing.get(0);
        ranking.setTotalScore(totalScore);
        ranking.setNumberOfExercises(numberOfExercises);
        ranking.setNumberOfCompletedExercises(numberOfCompletedExercises);
      }

      examRankingRepository.save(ranking);
    } catch (Exception e) {
      log.error(
        "Error upserting ExamRanking for ExamSubmission {}",
        updatedExamSubmission.getId(),
        e
      );
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
      .map(examRankingMapper::toExamRankingResponseDto)
      .collect(Collectors.toList());
  }
}
