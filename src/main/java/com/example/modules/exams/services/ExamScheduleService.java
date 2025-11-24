package com.example.modules.exams.services;

import com.example.modules.exams.entities.Exam;
import com.example.modules.exams.entities.ExamExercise;
import com.example.modules.exams.entities.ExamRanking;
import com.example.modules.exams.entities.ExamSubmission;
import com.example.modules.exams.repositories.ExamExerciseRepository;
import com.example.modules.exams.repositories.ExamRankingRepository;
import com.example.modules.exams.repositories.ExamSubmissionRepository;
import com.example.modules.exercises.entities.Exercise;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamScheduleService {

  private final ExamRankingRepository examRankingRepository;
  private final ExamSubmissionRepository examSubmissionRepository;
  private final ExamExerciseRepository examExerciseRepository;

  /**
   * Scheduled task chạy mỗi 1 phút để kiểm tra các bài exam của các bạn học sinh
   * Nếu quá thời gian limit -> tự động nộp bài
   * b1: tìm các bài exam ranking chưa hoàn thành (completed = false)
   * b2: kiểm tra thời gian hiện tại so với thời gian bắt đầu ( createdTimestamp) và so sánh với time limit
   * b3: nếu quá thời gian -> kiểm tra exam submission, nếu có bài chưa nộp thì tiến hành nộp bài
   * b4: tiến hành nộp bài ( tất cả những bài chưa nộp thì auto 0đ và ko có submissionId )
   */
  @Scheduled(cron = "0 * * * * *")
  @Transactional
  public void sheduledTask() {
    log.info(
      "SheduledTask to check exam (end time) running every minute in order to update exam submission"
    );

    // b1: find all exam rankings which are not completed
    List<ExamRanking> examRankings = examRankingRepository.findAll((root, query, cb) ->
      cb.and(cb.isFalse(root.get("completed")), cb.isNull(root.get("deletedTimestamp")))
    );
    log.info("Found {} exam rankings that are not completed", examRankings.size());

    Instant now = Instant.now();

    for (ExamRanking examRanking : examRankings) {
      try {
        processExamRanking(examRanking, now);
      } catch (Exception e) {
        log.error("Error processing exam ranking {}: {}", examRanking.getId(), e.getMessage(), e);
      }
    }
  }

  /**
   * Xử lý từng exam ranking:
   * - Kiểm tra thời gian limit
   * - Tự động nộp bài (score=0) cho các exercise chưa nộp
   * - Mark completed = true
   */
  private void processExamRanking(ExamRanking examRanking, Instant now) {
    log.info("Processing exam ranking with id: {}", examRanking.getId());

    Exam exam = examRanking.getExam();
    Double timeLimit = exam.getTimeLimit(); // timeLimit in minutes

    if (timeLimit == null || timeLimit <= 0) {
      log.warn("Exam {} has no valid time limit, skipping", exam.getId());
      return;
    }

    // b2: check if time limit exceeded
    Instant startTime = examRanking.getCreatedTimestamp(); // ex: 2025-11-14T10:00:00Z
    Instant deadline = startTime.plus(timeLimit.longValue() + 1, ChronoUnit.MINUTES); // ex: 2025-11-14T11:30:00Z with time limit 90 minutes + 1 phút buffer

    if (now.isBefore(deadline)) {
      log.debug("Exam ranking {} not yet expired (deadline: {})", examRanking.getId(), deadline);
      return;
    }

    log.info(
      "Exam ranking {} has exceeded time limit, processing submissions",
      examRanking.getId()
    );

    // b3: get all exercises in exam
    List<ExamExercise> examExercises = examExerciseRepository.findByExamId(exam.getId());
    int totalExercises = examExercises.size();

    // get already submitted exercises by user
    List<ExamSubmission> existingSubmissions = examSubmissionRepository.findByExamIdAndUserId(
      exam.getId(),
      examRanking.getUser().getId()
    );
    int submittedCount = existingSubmissions.size();

    // OPTION 3: Check if user already submitted all exercises
    if (submittedCount >= totalExercises) {
      log.info(
        "User {} already submitted all {} exercises for exam {}, just marking as completed",
        examRanking.getUser().getId(),
        totalExercises,
        exam.getId()
      );

      examRanking.setCompleted(true);
      examRankingRepository.save(examRanking);
      return; // No need to auto-submit
    }

    log.info(
      "User submitted {}/{} exercises, auto-submitting remaining exercises",
      submittedCount,
      totalExercises
    );

    Set<String> submittedExerciseIds = existingSubmissions
      .stream()
      .map(sub -> sub.getExercise().getId())
      .collect(Collectors.toSet());

    // b4: auto-submit unsubmitted exercises with score=0
    int autoSubmittedCount = 0;
    for (ExamExercise examExercise : examExercises) {
      Exercise exercise = examExercise.getExercise();

      if (!submittedExerciseIds.contains(exercise.getId())) {
        // create exam submission with score=0, no submissionId
        ExamSubmission autoSubmission = ExamSubmission.builder()
          .exam(exam)
          .user(examRanking.getUser())
          .exercise(exercise)
          .submissionId(null) // no actual submission
          .score(0.0) // auto 0 điểm
          .build();

        examSubmissionRepository.save(autoSubmission);
        autoSubmittedCount++;

        log.info(
          "Auto-submitted exercise {} for user {} with score 0",
          exercise.getId(),
          examRanking.getUser().getId()
        );
      }
    }

    // mark exam ranking as completed
    examRanking.setCompleted(true);
    examRankingRepository.save(examRanking);

    log.info(
      "Completed exam ranking {} - auto-submitted {} exercises",
      examRanking.getId(),
      autoSubmittedCount
    );
  }
}
