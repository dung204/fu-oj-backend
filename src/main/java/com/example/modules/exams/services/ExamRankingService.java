package com.example.modules.exams.services;

import com.example.modules.exams.dtos.ExamRankingCreateDTO;
import com.example.modules.exams.dtos.ExamRankingRequestDTO;
import com.example.modules.exams.dtos.ExamRankingResponseDTO;
import com.example.modules.exams.entities.*;
import com.example.modules.exams.exceptions.ExamEndedException;
import com.example.modules.exams.exceptions.ExamNotFoundException;
import com.example.modules.exams.exceptions.ExamNotStartedException;
import com.example.modules.exams.repositories.ExamExerciseRepository;
import com.example.modules.exams.repositories.ExamRankingRepository;
import com.example.modules.exams.repositories.ExamSubmissionRepository;
import com.example.modules.exams.repositories.GroupExamRepository;
import com.example.modules.exams.utils.ExamRankingMapper;
import com.example.modules.exams.utils.ExamRankingSpecification;
import com.example.modules.file.excel.utils.ExcelExporter;
import com.example.modules.submission_results.entities.SubmissionResult;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.submissions.enums.Verdict;
import com.example.modules.submissions.repositories.SubmissionsRepository;
import com.example.modules.users.entities.User;
import com.example.modules.users.exceptions.UserNotFoundException;
import com.example.modules.users.repositories.UsersRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
  private final GroupExamRepository groupExamRepository;
  private final ExamExerciseRepository examExerciseRepository;
  private final UsersRepository usersRepository;
  private final ExamRankingMapper examRankingMapper;
  private final SimpMessagingTemplate messagingTemplate;

  /**
   * Scheduled task chạy mỗi 1 phút để:
   *
   * <ul><li>Tìm các Submission là bài kiểm tra (isExamination = true), chưa có điểm (score = null)
   * và đã hoàn thành tất cả test cases → tính điểm và cập nhật ExamSubmission</li></ul>
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
              String.valueOf(Verdict.IN_QUEUE).equals(sr.getVerdict()) ||
              String.valueOf(Verdict.PROCESSING).equals(sr.getVerdict())
          );

        if (!allCompleted) {
          log.debug("Submission {} chưa hoàn thành hết test cases", submission.getId());
          continue;
        }

        // Tính passedTestCases
        long passedCount = results
          .stream()
          .filter(sr -> String.valueOf(Verdict.ACCEPTED).equals(sr.getVerdict()))
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
   * Cập nhật ExamRanking theo GroupExam của submission
   */
  private void upsertExamRanking(ExamSubmission updatedExamSubmission) {
    try {
      GroupExam groupExam = updatedExamSubmission.getGroupExam();
      if (groupExam == null || groupExam.getExam() == null) {
        log.warn(
          "ExamSubmission {} has no valid groupExam, skipping ranking update",
          updatedExamSubmission.getId()
        );
        return;
      }

      String groupExamId = groupExam.getId();
      String examId = groupExam.getExam().getId();
      String userId = updatedExamSubmission.getUser().getId();

      // Tính tổng điểm từ tất cả ExamSubmission của (groupExam, user)
      List<ExamSubmission> userExamSubmissions =
        examSubmissionRepository.findByGroupExamIdAndUserId(groupExamId, userId);

      // Get total exercises in exam (actual count from ExamExercise)
      List<ExamExercise> examExercises = examExerciseRepository.findByExamId(examId);
      double totalExercisesInExam = (double) examExercises.size();

      // Mỗi bài chỉ nộp 1 lần -> tính trực tiếp theo danh sách ExamSubmission
      double numberOfSubmittedExercises = (double) userExamSubmissions.size();
      double numberOfCompletedExercises = (double) userExamSubmissions
        .stream()
        .map(ExamSubmission::getScore)
        .filter(s -> s != null && s >= 100.0)
        .count();
      double totalScore = totalExercisesInExam > 0
        ? (numberOfCompletedExercises / totalExercisesInExam) * 100.0
        : 0.0;

      // Tìm hoặc tạo ExamRanking theo (groupExam, user)
      List<ExamRanking> existing = examRankingRepository.findAll((root, query, cb) ->
        cb.and(
          cb.equal(root.get("groupExam").get("id"), groupExamId),
          cb.equal(root.get("user").get("id"), userId)
        )
      );

      ExamRanking ranking;
      if (existing.isEmpty()) {
        ranking = ExamRanking.builder()
          .groupExam(groupExam)
          .user(updatedExamSubmission.getUser())
          .totalScore(totalScore)
          .numberOfExercises(totalExercisesInExam)
          .numberOfCompletedExercises(numberOfCompletedExercises)
          .build();
      } else {
        ranking = existing.get(0);
        ranking.setTotalScore(totalScore);
        ranking.setNumberOfCompletedExercises(numberOfCompletedExercises);
        ranking.setNumberOfExercises(totalExercisesInExam);
      }

      // Check if user submitted all exercises -> mark as completed
      if (numberOfSubmittedExercises >= totalExercisesInExam) {
        log.info(
          "User {} completed all {}/{} exercises for groupExam {}, marking as completed",
          userId,
          numberOfSubmittedExercises,
          totalExercisesInExam,
          groupExamId
        );
        ranking.setCompleted(true);
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

  public List<ExamRankingResponseDTO> getExamRankings(ExamRankingRequestDTO dto, User currentUser) {
    String effectiveUserId = dto.getUserId();
    if (currentUser != null && currentUser.getAccount() != null) {
      // Nếu là STUDENT thì chỉ được xem của chính mình
      var role = currentUser.getAccount().getRole();
      if (role != null && role.name().equals("STUDENT")) {
        effectiveUserId = currentUser.getId();
      }
    }

    var spec = ExamRankingSpecification.builder()
      .withGroupExamId(dto.getGroupExamId())
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

  /**
   * Tạo ExamRanking với chỉ groupExam và user, các field score để null
   * Scheduler sẽ tự động tính toán và cập nhật sau
   */
  @Transactional
  public ExamRankingResponseDTO createExamRanking(ExamRankingCreateDTO dto, User currentUser) {
    // 1. Validate groupExam exists
    GroupExam groupExam = groupExamRepository
      .findById(dto.getGroupExamId())
      .orElseThrow(() ->
        new ExamNotFoundException("GroupExam with id " + dto.getGroupExamId() + " not found")
      );

    // 2. Validate user exists
    User user = usersRepository
      .findById(dto.getUserId())
      .orElseThrow(() ->
        new UserNotFoundException("User with id " + dto.getUserId() + " not found")
      );

    // 2.1 validate time
    Exam exam = groupExam.getExam();
    Instant now = Instant.now();
    if (now.isBefore(exam.getStartTime())) {
      throw new ExamNotStartedException(
        "Thời gian làm bài tại: " + exam.getStartTime() + ", thời gian hiện tại: " + now
      );
    }
    if (now.isAfter(exam.getEndTime())) {
      throw new ExamEndedException(
        "Thời gian kết thúc tại: " + exam.getEndTime() + ", thời gian hiện tại: " + now
      );
    }

    // 3. Kiểm tra xem ExamRanking đã tồn tại chưa
    List<ExamRanking> existing = examRankingRepository.findAll((root, query, cb) ->
      cb.and(
        cb.equal(root.get("groupExam").get("id"), dto.getGroupExamId()),
        cb.equal(root.get("user").get("id"), dto.getUserId()),
        cb.isNull(root.get("deletedTimestamp"))
      )
    );

    if (!existing.isEmpty()) {
      log.info(
        "ExamRanking already exists for groupExam {} and user {}",
        dto.getGroupExamId(),
        dto.getUserId()
      );
      return examRankingMapper.toExamRankingResponseDto(existing.get(0));
    }

    // 4. Tạo ExamRanking mới với các field score = null
    ExamRanking ranking = ExamRanking.builder()
      .groupExam(groupExam)
      .user(user)
      .totalScore(null)
      .numberOfExercises(dto.getNumberOfExercises())
      .numberOfCompletedExercises(null)
      .completed(false)
      .build();

    ranking = examRankingRepository.save(ranking);

    log.info(
      "Created ExamRanking for groupExam {} and user {}",
      dto.getGroupExamId(),
      dto.getUserId()
    );

    // Map to DTO for response and WebSocket
    ExamRankingResponseDTO responseDTO = examRankingMapper.toExamRankingResponseDto(ranking);

    // Send WebSocket notification to topic
    String topic = "/topic/exam/groupExam/" + dto.getGroupExamId();
    messagingTemplate.convertAndSend(topic, responseDTO);
    log.info(
      "Sent WebSocket notification to {} for new ExamRanking: user={}, totalScore={}",
      topic,
      responseDTO.getUser().getEmail(),
      responseDTO.getTotalScore()
    );

    return responseDTO;
  }

  /**
   * Export exam rankings to Excel by groupExamId
   */
  public byte[] exportExamRankingsToExcel(String groupExamId) throws IOException {
    // Tìm tất cả exam rankings theo groupExamId
    var spec = ExamRankingSpecification.builder().withGroupExamId(groupExamId).notDeleted().build();

    List<ExamRanking> rankings = examRankingRepository.findAll(spec);

    // Chuyển đổi sang DTO flatten để export
    List<ExamRankingExportDTO> exportData = rankings
      .stream()
      .map(ranking -> {
        User user = ranking.getUser();
        return ExamRankingExportDTO.builder()
          .rollNumber(user.getRollNumber() != null ? user.getRollNumber() : "")
          .email(user.getAccount() != null ? user.getAccount().getEmail() : "")
          .firstName(user.getFirstName() != null ? user.getFirstName() : "")
          .lastName(user.getLastName() != null ? user.getLastName() : "")
          .totalScore(ranking.getTotalScore() != null ? ranking.getTotalScore() : 0.0)
          .numberOfCompletedExercises(
            ranking.getNumberOfCompletedExercises() != null
              ? ranking.getNumberOfCompletedExercises()
              : 0.0
          )
          .numberOfExercises(
            ranking.getNumberOfExercises() != null ? ranking.getNumberOfExercises() : 0.0
          )
          .completed(ranking.getCompleted() ? "Yes" : "No")
          .build();
      })
      .collect(Collectors.toList());

    // Định nghĩa headers và fields để export
    String[] headers = {
      "Roll Number",
      "Email",
      "First Name",
      "Last Name",
      "Total Score",
      "Completed Exercises",
      "Total Exercises",
      "Completed",
    };
    String[] fieldNames = {
      "rollNumber",
      "email",
      "firstName",
      "lastName",
      "totalScore",
      "numberOfCompletedExercises",
      "numberOfExercises",
      "completed",
    };

    // Export to Excel
    ExcelExporter<ExamRankingExportDTO> exporter = new ExcelExporter<>();
    return exporter.export(exportData, "Exam Rankings", headers, fieldNames);
  }

  /**
   * DTO để export exam ranking ra Excel
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ExamRankingExportDTO {

    private String rollNumber;
    private String email;
    private String firstName;
    private String lastName;
    private Double totalScore;
    private Double numberOfCompletedExercises;
    private Double numberOfExercises;
    private String completed;
  }
}
