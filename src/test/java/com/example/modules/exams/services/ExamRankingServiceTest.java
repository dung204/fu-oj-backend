package com.example.modules.exams.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.base.BaseServiceTest;
import com.example.modules.auth.enums.Role;
import com.example.modules.exams.dtos.ExamRankingCreateDTO;
import com.example.modules.exams.dtos.ExamRankingRequestDTO;
import com.example.modules.exams.dtos.ExamRankingResponseDTO;
import com.example.modules.exams.entities.Exam;
import com.example.modules.exams.entities.ExamExercise;
import com.example.modules.exams.entities.ExamRanking;
import com.example.modules.exams.entities.ExamSubmission;
import com.example.modules.exams.exceptions.ExamNotFoundException;
import com.example.modules.exams.repositories.ExamExerciseRepository;
import com.example.modules.exams.repositories.ExamRankingRepository;
import com.example.modules.exams.repositories.ExamRepository;
import com.example.modules.exams.repositories.ExamSubmissionRepository;
import com.example.modules.exams.utils.ExamRankingMapper;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.submission_results.entities.SubmissionResult;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.submissions.enums.Verdict;
import com.example.modules.submissions.repositories.SubmissionsRepository;
import com.example.modules.users.entities.User;
import com.example.modules.users.exceptions.UserNotFoundException;
import com.example.modules.users.repositories.UsersRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.jpa.domain.Specification;

@SuppressWarnings("unchecked")
public class ExamRankingServiceTest extends BaseServiceTest {

  @Mock
  private SubmissionsRepository submissionsRepository;

  @Mock
  private ExamSubmissionRepository examSubmissionRepository;

  @Mock
  private ExamRankingRepository examRankingRepository;

  @Mock
  private ExamRepository examRepository;

  @Mock
  private ExamExerciseRepository examExerciseRepository;

  @Mock
  private UsersRepository usersRepository;

  @Mock
  private ExamRankingMapper examRankingMapper;

  @InjectMocks
  private ExamRankingService examRankingService;

  private ExamSubmission getMockExamSubmission() {
    return ExamSubmission.builder()
      .id("exam-sub-1")
      .exam(Exam.builder().id("exam-123").build())
      .user(getMockUser())
      .exercise(Exercise.builder().id("exercise-1").build())
      .submissionId("submission-1")
      .score(null)
      .build();
  }

  private Submission getMockSubmission(List<SubmissionResult> results) {
    Submission submission = new Submission();
    submission.setId("submission-1");
    submission.setSubmissionResults(results);
    return submission;
  }

  private User getMockUserWithRole(Role role) {
    User user = getMockUser();
    user.getAccount().setRole(role);
    return user;
  }

  @Test
  void calculateExamSubmissionsScore_WhenAllTestCasesCompleted_ShouldUpdateScore() {
    // Given
    ExamSubmission examSubmission = getMockExamSubmission();

    SubmissionResult result1 = new SubmissionResult();
    result1.setVerdict(String.valueOf(Verdict.ACCEPTED));

    SubmissionResult result2 = new SubmissionResult();
    result2.setVerdict(String.valueOf(Verdict.ACCEPTED));

    Submission submission = getMockSubmission(List.of(result1, result2));

    when(examSubmissionRepository.findAll(any(Specification.class))).thenReturn(
      List.of(examSubmission)
    );
    when(submissionsRepository.findById(examSubmission.getSubmissionId())).thenReturn(
      Optional.of(submission)
    );
    when(examSubmissionRepository.save(any(ExamSubmission.class))).thenReturn(examSubmission);

    // Mock ExamRanking data
    when(examSubmissionRepository.findByExamIdAndUserId(anyString(), anyString())).thenReturn(
      List.of(examSubmission)
    );
    when(examExerciseRepository.findByExamId(anyString())).thenReturn(
      List.of(ExamExercise.builder().id("ee-1").build())
    );
    when(examRankingRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());
    when(examRankingRepository.save(any(ExamRanking.class))).thenReturn(
      ExamRanking.builder().build()
    );

    // When
    examRankingService.calculateExamSubmissionsScore();

    // Then
    verify(examSubmissionRepository).save(any(ExamSubmission.class));
    verify(examRankingRepository).save(any(ExamRanking.class));
  }

  @Test
  void calculateExamSubmissionsScore_WhenSubmissionInQueue_ShouldSkip() {
    // Given
    ExamSubmission examSubmission = getMockExamSubmission();

    SubmissionResult result1 = new SubmissionResult();
    result1.setVerdict(String.valueOf(Verdict.IN_QUEUE));

    Submission submission = getMockSubmission(List.of(result1));

    when(examSubmissionRepository.findAll(any(Specification.class))).thenReturn(
      List.of(examSubmission)
    );
    when(submissionsRepository.findById(examSubmission.getSubmissionId())).thenReturn(
      Optional.of(submission)
    );

    // When
    examRankingService.calculateExamSubmissionsScore();

    // Then
    verify(examSubmissionRepository, never()).save(any(ExamSubmission.class));
  }

  @Test
  void calculateExamSubmissionsScore_WhenPartiallyAccepted_ShouldCalculateProportionalScore() {
    // Given
    ExamSubmission examSubmission = getMockExamSubmission();

    SubmissionResult result1 = new SubmissionResult();
    result1.setVerdict(String.valueOf(Verdict.ACCEPTED));

    SubmissionResult result2 = new SubmissionResult();
    result2.setVerdict(String.valueOf(Verdict.WRONG_ANSWER));

    SubmissionResult result3 = new SubmissionResult();
    result3.setVerdict(String.valueOf(Verdict.TIME_LIMIT_EXCEEDED));

    SubmissionResult result4 = new SubmissionResult();
    result4.setVerdict(String.valueOf(Verdict.ACCEPTED));

    Submission submission = getMockSubmission(List.of(result1, result2, result3, result4));

    when(examSubmissionRepository.findAll(any(Specification.class))).thenReturn(
      List.of(examSubmission)
    );
    when(submissionsRepository.findById(examSubmission.getSubmissionId())).thenReturn(
      Optional.of(submission)
    );
    when(examSubmissionRepository.save(any(ExamSubmission.class))).thenReturn(examSubmission);

    // Mock ExamRanking data
    when(examSubmissionRepository.findByExamIdAndUserId(anyString(), anyString())).thenReturn(
      List.of(examSubmission)
    );
    when(examExerciseRepository.findByExamId(anyString())).thenReturn(
      List.of(ExamExercise.builder().id("ee-1").build())
    );
    when(examRankingRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());
    when(examRankingRepository.save(any(ExamRanking.class))).thenReturn(
      ExamRanking.builder().build()
    );

    // When
    examRankingService.calculateExamSubmissionsScore();

    // Then
    verify(examSubmissionRepository).save(
      argThat(es -> {
        // 2 passed out of 4 = 50%
        return es.getScore() != null && es.getScore() == 50.0;
      })
    );
  }

  @Test
  void getExamRankings_WhenStudentUser_ShouldReturnOnlyOwnRanking() {
    // Given
    User student = getMockUserWithRole(Role.STUDENT);
    ExamRankingRequestDTO dto = new ExamRankingRequestDTO();
    dto.setExamId("exam-123");
    dto.setUserId("other-user"); // Try to access other user

    ExamRanking ranking = ExamRanking.builder()
      .id("ranking-1")
      .exam(Exam.builder().id("exam-123").build())
      .user(student)
      .totalScore(85.0)
      .build();

    ExamRankingResponseDTO responseDTO = ExamRankingResponseDTO.builder().id("ranking-1").build();

    when(examRankingRepository.findAll(any(Specification.class))).thenReturn(List.of(ranking));
    when(examRankingMapper.toExamRankingResponseDto(ranking)).thenReturn(responseDTO);

    // When
    List<ExamRankingResponseDTO> result = examRankingService.getExamRankings(dto, student);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(examRankingRepository).findAll(any(Specification.class));
  }

  @Test
  void createExamRanking_WhenValidData_ShouldCreateRanking() {
    // Given
    ExamRankingCreateDTO dto = new ExamRankingCreateDTO();
    dto.setExamId("exam-123");
    dto.setUserId("user-123");
    dto.setNumberOfExercises(5.0);

    Exam exam = Exam.builder().id("exam-123").build();
    User user = getMockUser();

    ExamRanking ranking = ExamRanking.builder()
      .id("ranking-1")
      .exam(exam)
      .user(user)
      .totalScore(null)
      .numberOfExercises(5.0)
      .build();

    ExamRankingResponseDTO responseDTO = ExamRankingResponseDTO.builder().id("ranking-1").build();

    when(examRepository.findById(dto.getExamId())).thenReturn(Optional.of(exam));
    when(usersRepository.findById(dto.getUserId())).thenReturn(Optional.of(user));
    when(examRankingRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());
    when(examRankingRepository.save(any(ExamRanking.class))).thenReturn(ranking);
    when(examRankingMapper.toExamRankingResponseDto(ranking)).thenReturn(responseDTO);

    // When
    ExamRankingResponseDTO result = examRankingService.createExamRanking(dto, user);

    // Then
    assertNotNull(result);
    verify(examRankingRepository).save(any(ExamRanking.class));
  }

  @Test
  void createExamRanking_WhenExamNotFound_ShouldThrowException() {
    // Given
    ExamRankingCreateDTO dto = new ExamRankingCreateDTO();
    dto.setExamId("non-existent");
    dto.setUserId("user-123");

    when(examRepository.findById(dto.getExamId())).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ExamNotFoundException.class, () ->
      examRankingService.createExamRanking(dto, getMockUser())
    );
    verify(examRankingRepository, never()).save(any(ExamRanking.class));
  }

  @Test
  void createExamRanking_WhenUserNotFound_ShouldThrowException() {
    // Given
    ExamRankingCreateDTO dto = new ExamRankingCreateDTO();
    dto.setExamId("exam-123");
    dto.setUserId("non-existent");

    Exam exam = Exam.builder().id("exam-123").build();

    when(examRepository.findById(dto.getExamId())).thenReturn(Optional.of(exam));
    when(usersRepository.findById(dto.getUserId())).thenReturn(Optional.empty());

    // When & Then
    assertThrows(UserNotFoundException.class, () ->
      examRankingService.createExamRanking(dto, getMockUser())
    );
    verify(examRankingRepository, never()).save(any(ExamRanking.class));
  }

  @Test
  void createExamRanking_WhenRankingAlreadyExists_ShouldReturnExisting() {
    // Given
    ExamRankingCreateDTO dto = new ExamRankingCreateDTO();
    dto.setExamId("exam-123");
    dto.setUserId("user-123");

    Exam exam = Exam.builder().id("exam-123").build();
    User user = getMockUser();

    ExamRanking existingRanking = ExamRanking.builder()
      .id("ranking-1")
      .exam(exam)
      .user(user)
      .build();

    ExamRankingResponseDTO responseDTO = ExamRankingResponseDTO.builder().id("ranking-1").build();

    when(examRepository.findById(dto.getExamId())).thenReturn(Optional.of(exam));
    when(usersRepository.findById(dto.getUserId())).thenReturn(Optional.of(user));
    when(examRankingRepository.findAll(any(Specification.class))).thenReturn(
      List.of(existingRanking)
    );
    when(examRankingMapper.toExamRankingResponseDto(existingRanking)).thenReturn(responseDTO);

    // When
    ExamRankingResponseDTO result = examRankingService.createExamRanking(dto, user);

    // Then
    assertNotNull(result);
    verify(examRankingRepository, never()).save(any(ExamRanking.class));
  }
}
