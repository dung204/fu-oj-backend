package com.example.modules.exams.services;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.base.BaseServiceTest;
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
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.jpa.domain.Specification;

@SuppressWarnings("unchecked")
public class ExamScheduleServiceTest extends BaseServiceTest {

  @Mock
  private ExamRankingRepository examRankingRepository;

  @Mock
  private ExamSubmissionRepository examSubmissionRepository;

  @Mock
  private ExamExerciseRepository examExerciseRepository;

  @InjectMocks
  private ExamScheduleService examScheduleService;

  private ExamRanking getMockExamRanking(Double timeLimit, Instant createdTime) {
    return ExamRanking.builder()
      .id("ranking-1")
      .exam(Exam.builder().id("exam-123").timeLimit(timeLimit).build())
      .user(getMockUser())
      .completed(false)
      .createdTimestamp(createdTime)
      .build();
  }

  @Test
  void sheduledTask_WhenTimeLimitExceeded_ShouldAutoSubmitAndMarkCompleted() {
    // Given
    Instant now = Instant.now();
    Instant startTime = now.minus(100, ChronoUnit.MINUTES); // Started 100 min ago
    ExamRanking ranking = getMockExamRanking(90.0, startTime); // 90 min limit

    Exercise exercise1 = Exercise.builder().id("exercise-1").build();
    Exercise exercise2 = Exercise.builder().id("exercise-2").build();

    ExamExercise ee1 = ExamExercise.builder().id("ee-1").exercise(exercise1).build();
    ExamExercise ee2 = ExamExercise.builder().id("ee-2").exercise(exercise2).build();

    // User has submitted only 1 exercise
    ExamSubmission existingSub = ExamSubmission.builder().id("sub-1").exercise(exercise1).build();

    when(examRankingRepository.findAll(any(Specification.class))).thenReturn(List.of(ranking));
    when(examExerciseRepository.findByExamId("exam-123")).thenReturn(List.of(ee1, ee2));
    when(examSubmissionRepository.findByExamIdAndUserId(anyString(), anyString())).thenReturn(
      List.of(existingSub)
    );
    when(examSubmissionRepository.save(any(ExamSubmission.class))).thenReturn(
      ExamSubmission.builder().build()
    );
    when(examRankingRepository.save(any(ExamRanking.class))).thenReturn(ranking);

    // When
    examScheduleService.sheduledTask();

    // Then
    verify(examSubmissionRepository).save(
      argThat(submission -> {
        // Should auto-submit exercise-2 with score 0
        return (
          submission.getScore() != null &&
          submission.getScore() == 0.0 &&
          submission.getSubmissionId() == null
        );
      })
    );
    verify(examRankingRepository).save(argThat(r -> r.getCompleted() == true));
  }

  @Test
  void sheduledTask_WhenNotYetExpired_ShouldNotAutoSubmit() {
    // Given
    Instant now = Instant.now();
    Instant startTime = now.minus(30, ChronoUnit.MINUTES); // Started 30 min ago
    ExamRanking ranking = getMockExamRanking(90.0, startTime); // 90 min limit, still valid

    when(examRankingRepository.findAll(any(Specification.class))).thenReturn(List.of(ranking));

    // When
    examScheduleService.sheduledTask();

    // Then
    verify(examSubmissionRepository, never()).save(any(ExamSubmission.class));
    verify(examRankingRepository, never()).save(any(ExamRanking.class));
  }

  @Test
  void sheduledTask_WhenUserSubmittedAllExercises_ShouldOnlyMarkCompleted() {
    // Given
    Instant now = Instant.now();
    Instant startTime = now.minus(100, ChronoUnit.MINUTES);
    ExamRanking ranking = getMockExamRanking(90.0, startTime);

    Exercise exercise1 = Exercise.builder().id("exercise-1").build();
    Exercise exercise2 = Exercise.builder().id("exercise-2").build();

    ExamExercise ee1 = ExamExercise.builder().id("ee-1").exercise(exercise1).build();
    ExamExercise ee2 = ExamExercise.builder().id("ee-2").exercise(exercise2).build();

    // User has submitted both exercises
    ExamSubmission sub1 = ExamSubmission.builder().id("sub-1").exercise(exercise1).build();
    ExamSubmission sub2 = ExamSubmission.builder().id("sub-2").exercise(exercise2).build();

    when(examRankingRepository.findAll(any(Specification.class))).thenReturn(List.of(ranking));
    when(examExerciseRepository.findByExamId("exam-123")).thenReturn(List.of(ee1, ee2));
    when(examSubmissionRepository.findByExamIdAndUserId(anyString(), anyString())).thenReturn(
      List.of(sub1, sub2)
    );
    when(examRankingRepository.save(any(ExamRanking.class))).thenReturn(ranking);

    // When
    examScheduleService.sheduledTask();

    // Then
    verify(examSubmissionRepository, never()).save(any(ExamSubmission.class));
    verify(examRankingRepository).save(argThat(r -> r.getCompleted() == true));
  }

  @Test
  void sheduledTask_WhenNoTimeLimitSet_ShouldSkip() {
    // Given
    Instant now = Instant.now();
    ExamRanking ranking = getMockExamRanking(null, now.minus(100, ChronoUnit.MINUTES));

    when(examRankingRepository.findAll(any(Specification.class))).thenReturn(List.of(ranking));

    // When
    examScheduleService.sheduledTask();

    // Then
    verify(examSubmissionRepository, never()).findByExamIdAndUserId(anyString(), anyString());
    verify(examRankingRepository, never()).save(any(ExamRanking.class));
  }

  @Test
  void sheduledTask_WhenTimeLimitIsZero_ShouldSkip() {
    // Given
    Instant now = Instant.now();
    ExamRanking ranking = getMockExamRanking(0.0, now.minus(100, ChronoUnit.MINUTES));

    when(examRankingRepository.findAll(any(Specification.class))).thenReturn(List.of(ranking));

    // When
    examScheduleService.sheduledTask();

    // Then
    verify(examSubmissionRepository, never()).findByExamIdAndUserId(anyString(), anyString());
    verify(examRankingRepository, never()).save(any(ExamRanking.class));
  }

  @Test
  void sheduledTask_WhenNoUncompletedRankings_ShouldDoNothing() {
    // Given
    when(examRankingRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());

    // When
    examScheduleService.sheduledTask();

    // Then
    verify(examSubmissionRepository, never()).save(any(ExamSubmission.class));
    verify(examRankingRepository, never()).save(any(ExamRanking.class));
  }

  @Test
  void sheduledTask_WhenMultipleUnsubmittedExercises_ShouldAutoSubmitAll() {
    // Given
    Instant now = Instant.now();
    Instant startTime = now.minus(100, ChronoUnit.MINUTES);
    ExamRanking ranking = getMockExamRanking(90.0, startTime);

    Exercise exercise1 = Exercise.builder().id("exercise-1").build();
    Exercise exercise2 = Exercise.builder().id("exercise-2").build();
    Exercise exercise3 = Exercise.builder().id("exercise-3").build();

    ExamExercise ee1 = ExamExercise.builder().id("ee-1").exercise(exercise1).build();
    ExamExercise ee2 = ExamExercise.builder().id("ee-2").exercise(exercise2).build();
    ExamExercise ee3 = ExamExercise.builder().id("ee-3").exercise(exercise3).build();

    // User hasn't submitted anything
    when(examRankingRepository.findAll(any(Specification.class))).thenReturn(List.of(ranking));
    when(examExerciseRepository.findByExamId("exam-123")).thenReturn(List.of(ee1, ee2, ee3));
    when(examSubmissionRepository.findByExamIdAndUserId(anyString(), anyString())).thenReturn(
      new ArrayList<>()
    );
    when(examSubmissionRepository.save(any(ExamSubmission.class))).thenReturn(
      ExamSubmission.builder().build()
    );
    when(examRankingRepository.save(any(ExamRanking.class))).thenReturn(ranking);

    // When
    examScheduleService.sheduledTask();

    // Then
    // Should auto-submit 3 exercises
    verify(examSubmissionRepository, times(3)).save(any(ExamSubmission.class));
    verify(examRankingRepository).save(argThat(r -> r.getCompleted() == true));
  }
}
