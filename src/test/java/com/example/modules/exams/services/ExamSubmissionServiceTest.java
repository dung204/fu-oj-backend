package com.example.modules.exams.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.base.BaseServiceTest;
import com.example.modules.auth.enums.Role;
import com.example.modules.exams.dtos.ExamResultRequestDTO;
import com.example.modules.exams.dtos.ExamResultResponseDTO;
import com.example.modules.exams.dtos.ExamSubmissionCreateDTO;
import com.example.modules.exams.dtos.ExamSubmissionResponseDTO;
import com.example.modules.exams.entities.Exam;
import com.example.modules.exams.entities.ExamExercise;
import com.example.modules.exams.entities.ExamSubmission;
import com.example.modules.exams.entities.GroupExam;
import com.example.modules.exams.exceptions.*;
import com.example.modules.exams.repositories.ExamRepository;
import com.example.modules.exams.repositories.ExamSubmissionRepository;
import com.example.modules.exams.repositories.GroupExamRepository;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.exceptions.ExerciseNotFoundException;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.groups.entities.Group;
import com.example.modules.submissions.dtos.SubmissionResponseDTO;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.submissions.repositories.SubmissionsRepository;
import com.example.modules.submissions.services.SubmissionsService;
import com.example.modules.users.entities.User;
import com.example.modules.users.repositories.UsersRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.jpa.domain.Specification;

@SuppressWarnings("unchecked")
public class ExamSubmissionServiceTest extends BaseServiceTest {

  @Mock
  private ExamRepository examRepository;

  @Mock
  private ExamSubmissionRepository examSubmissionRepository;

  @Mock
  private GroupExamRepository groupExamRepository;

  @Mock
  private ExercisesRepository exercisesRepository;

  @Mock
  private SubmissionsService submissionsService;

  @Mock
  private SubmissionsRepository submissionsRepository;

  @Mock
  private UsersRepository usersRepository;

  @InjectMocks
  private ExamSubmissionService examSubmissionService;

  private Exam getMockExam() {
    Instant now = Instant.now();
    return Exam.builder()
      .id("exam-123")
      .code("EXAM-ABC123")
      .startTime(now.minus(10, ChronoUnit.MINUTES))
      .endTime(now.plus(50, ChronoUnit.MINUTES))
      .timeLimit(90.0)
      .examExercises(
        List.of(
          ExamExercise.builder().exercise(Exercise.builder().id("exercise-1").build()).build(),
          ExamExercise.builder().exercise(Exercise.builder().id("exercise-2").build()).build()
        )
      )
      .build();
  }

  private User getMockUserWithRole(Role role) {
    User user = getMockUser();
    user.getAccount().setRole(role);
    return user;
  }

  @Test
  void createExamSubmission_WhenValidSubmission_ShouldCreateSuccessfully() {
    // Given
    User student = getMockUserWithRole(Role.STUDENT);
    Exam exam = getMockExam();
    Exercise exercise = Exercise.builder().id("exercise-1").build();

    Group group = Group.builder().id("group-1").students(List.of(student)).build();
    GroupExam groupExam = GroupExam.builder().exam(exam).group(group).build();

    ExamSubmissionCreateDTO dto = new ExamSubmissionCreateDTO();
    dto.setExamId("exam-123");
    dto.setExerciseId("exercise-1");
    dto.setSourceCode("print('Hello World')");
    dto.setLanguageCode("python");

    SubmissionResponseDTO submissionResponse = SubmissionResponseDTO.builder()
      .id("submission-1")
      .build();

    ExamSubmission savedExamSubmission = ExamSubmission.builder()
      .id("exam-sub-1")
      .exam(exam)
      .user(student)
      .exercise(exercise)
      .submissionId("submission-1")
      .build();

    when(examRepository.findById("exam-123")).thenReturn(Optional.of(exam));
    when(groupExamRepository.findByExamId("exam-123")).thenReturn(List.of(groupExam));
    when(exercisesRepository.findById("exercise-1")).thenReturn(Optional.of(exercise));
    when(
      examSubmissionRepository.findByExamIdAndUserIdAndExerciseId(
        anyString(),
        anyString(),
        anyString()
      )
    ).thenReturn(new ArrayList<>());
    when(submissionsService.createSubmissionBase64(any(), any())).thenReturn(submissionResponse);
    when(examSubmissionRepository.save(any(ExamSubmission.class))).thenReturn(savedExamSubmission);

    // When
    ExamSubmissionResponseDTO result = examSubmissionService.createExamSubmission(dto, student);

    // Then
    assertNotNull(result);
    assertEquals("exam-sub-1", result.getId());
    verify(submissionsService).createSubmissionBase64(any(), any());
    verify(examSubmissionRepository).save(any(ExamSubmission.class));
  }

  @Test
  void createExamSubmission_WhenExamNotStarted_ShouldThrowException() {
    // Given
    User student = getMockUserWithRole(Role.STUDENT);
    Instant now = Instant.now();

    Exam exam = Exam.builder()
      .id("exam-123")
      .startTime(now.plus(10, ChronoUnit.MINUTES)) // Future start time
      .endTime(now.plus(100, ChronoUnit.MINUTES))
      .build();

    ExamSubmissionCreateDTO dto = new ExamSubmissionCreateDTO();
    dto.setExamId("exam-123");
    dto.setExerciseId("exercise-1");

    when(examRepository.findById("exam-123")).thenReturn(Optional.of(exam));

    // When & Then
    assertThrows(ExamNotStartedException.class, () ->
      examSubmissionService.createExamSubmission(dto, student)
    );
    verify(examSubmissionRepository, never()).save(any(ExamSubmission.class));
  }

  @Test
  void createExamSubmission_WhenExamEnded_ShouldThrowException() {
    // Given
    User student = getMockUserWithRole(Role.STUDENT);
    Instant now = Instant.now();

    Exam exam = Exam.builder()
      .id("exam-123")
      .startTime(now.minus(100, ChronoUnit.MINUTES))
      .endTime(now.minus(10, ChronoUnit.MINUTES)) // Past end time
      .build();

    ExamSubmissionCreateDTO dto = new ExamSubmissionCreateDTO();
    dto.setExamId("exam-123");
    dto.setExerciseId("exercise-1");

    when(examRepository.findById("exam-123")).thenReturn(Optional.of(exam));

    // When & Then
    assertThrows(ExamEndedException.class, () ->
      examSubmissionService.createExamSubmission(dto, student)
    );
    verify(examSubmissionRepository, never()).save(any(ExamSubmission.class));
  }

  @Test
  void createExamSubmission_WhenStudentNotInGroup_ShouldThrowException() {
    // Given
    User student = getMockUserWithRole(Role.STUDENT);
    User otherStudent = getMockUser();
    otherStudent.setId("other-user");

    Exam exam = getMockExam();
    Group group = Group.builder().id("group-1").students(List.of(otherStudent)).build();
    GroupExam groupExam = GroupExam.builder().exam(exam).group(group).build();

    ExamSubmissionCreateDTO dto = new ExamSubmissionCreateDTO();
    dto.setExamId("exam-123");
    dto.setExerciseId("exercise-1");

    when(examRepository.findById("exam-123")).thenReturn(Optional.of(exam));
    when(groupExamRepository.findByExamId("exam-123")).thenReturn(List.of(groupExam));

    // When & Then
    assertThrows(StudentNotInGroupException.class, () ->
      examSubmissionService.createExamSubmission(dto, student)
    );
    verify(examSubmissionRepository, never()).save(any(ExamSubmission.class));
  }

  @Test
  void createExamSubmission_WhenExerciseNotInExam_ShouldThrowException() {
    // Given
    User student = getMockUserWithRole(Role.STUDENT);
    Exam exam = getMockExam();
    Exercise exercise = Exercise.builder().id("exercise-999").build(); // Not in exam

    Group group = Group.builder().id("group-1").students(List.of(student)).build();
    GroupExam groupExam = GroupExam.builder().exam(exam).group(group).build();

    ExamSubmissionCreateDTO dto = new ExamSubmissionCreateDTO();
    dto.setExamId("exam-123");
    dto.setExerciseId("exercise-999");

    when(examRepository.findById("exam-123")).thenReturn(Optional.of(exam));
    when(groupExamRepository.findByExamId("exam-123")).thenReturn(List.of(groupExam));
    when(exercisesRepository.findById("exercise-999")).thenReturn(Optional.of(exercise));

    // When & Then
    assertThrows(ExerciseNotInExamException.class, () ->
      examSubmissionService.createExamSubmission(dto, student)
    );
    verify(examSubmissionRepository, never()).save(any(ExamSubmission.class));
  }

  @Test
  void createExamSubmission_WhenExerciseNotFound_ShouldThrowException() {
    // Given
    User student = getMockUserWithRole(Role.STUDENT);
    Exam exam = getMockExam();

    Group group = Group.builder().id("group-1").students(List.of(student)).build();
    GroupExam groupExam = GroupExam.builder().exam(exam).group(group).build();

    ExamSubmissionCreateDTO dto = new ExamSubmissionCreateDTO();
    dto.setExamId("exam-123");
    dto.setExerciseId("non-existent");

    when(examRepository.findById("exam-123")).thenReturn(Optional.of(exam));
    when(groupExamRepository.findByExamId("exam-123")).thenReturn(List.of(groupExam));
    when(exercisesRepository.findById("non-existent")).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ExerciseNotFoundException.class, () ->
      examSubmissionService.createExamSubmission(dto, student)
    );
    verify(examSubmissionRepository, never()).save(any(ExamSubmission.class));
  }

  @Test
  void createExamSubmission_WhenDuplicateSubmission_ShouldThrowException() {
    // Given
    User student = getMockUserWithRole(Role.STUDENT);
    Exam exam = getMockExam();
    Exercise exercise = Exercise.builder().id("exercise-1").build();

    Group group = Group.builder().id("group-1").students(List.of(student)).build();
    GroupExam groupExam = GroupExam.builder().exam(exam).group(group).build();

    ExamSubmissionCreateDTO dto = new ExamSubmissionCreateDTO();
    dto.setExamId("exam-123");
    dto.setExerciseId("exercise-1");

    ExamSubmission existingSubmission = ExamSubmission.builder().id("existing-1").build();

    when(examRepository.findById("exam-123")).thenReturn(Optional.of(exam));
    when(groupExamRepository.findByExamId("exam-123")).thenReturn(List.of(groupExam));
    when(exercisesRepository.findById("exercise-1")).thenReturn(Optional.of(exercise));
    when(
      examSubmissionRepository.findByExamIdAndUserIdAndExerciseId(
        anyString(),
        anyString(),
        anyString()
      )
    ).thenReturn(List.of(existingSubmission));

    // When & Then
    assertThrows(DuplicateExamSubmissionException.class, () ->
      examSubmissionService.createExamSubmission(dto, student)
    );
    verify(submissionsService, never()).createSubmissionBase64(any(), any());
  }

  @Test
  void getExamResult_WhenStudentAccessOwnResult_ShouldReturnResult() {
    // Given
    User student = getMockUserWithRole(Role.STUDENT);
    Exam exam = getMockExam();

    ExamResultRequestDTO dto = new ExamResultRequestDTO();
    dto.setExamId("exam-123");
    dto.setUserId(student.getId());

    Exercise exercise = Exercise.builder().id("exercise-1").title("Exercise 1").code("EX1").build();

    ExamSubmission examSubmission = ExamSubmission.builder()
      .id("exam-sub-1")
      .exercise(exercise)
      .submissionId("submission-1")
      .build();

    Submission submission = new Submission();
    submission.setId("submission-1");
    submission.setScore(100.0);
    submission.setIsAccepted(true);
    submission.setPassedTestCases(10);
    submission.setTotalTestCases(10);

    when(examRepository.findById("exam-123")).thenReturn(Optional.of(exam));
    when(usersRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(examSubmissionRepository.findAll(any(Specification.class))).thenReturn(
      List.of(examSubmission)
    );
    when(submissionsRepository.findById("submission-1")).thenReturn(Optional.of(submission));

    // When
    ExamResultResponseDTO result = examSubmissionService.getExamResult(dto, student);

    // Then
    assertNotNull(result);
    assertEquals("exam-123", result.getExamId());
    assertEquals(1, result.getSubmissions().size());
    verify(examSubmissionRepository).findAll(any(Specification.class));
  }

  @Test
  void getExamResult_WhenStudentAccessOtherStudentResult_ShouldForceOwnUserId() {
    // Given
    User student = getMockUserWithRole(Role.STUDENT);
    Exam exam = getMockExam();

    ExamResultRequestDTO dto = new ExamResultRequestDTO();
    dto.setExamId("exam-123");
    dto.setUserId("other-user"); // Try to access other user

    when(examRepository.findById("exam-123")).thenReturn(Optional.of(exam));
    when(usersRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(examSubmissionRepository.findAll(any(Specification.class))).thenReturn(new ArrayList<>());

    // When
    ExamResultResponseDTO result = examSubmissionService.getExamResult(dto, student);

    // Then
    assertNotNull(result);
    assertEquals(student.getId(), result.getUserId());
  }

  @Test
  void getExamResult_WhenAutoSubmittedExercise_ShouldShowZeroScore() {
    // Given
    User student = getMockUserWithRole(Role.STUDENT);
    Exam exam = getMockExam();

    ExamResultRequestDTO dto = new ExamResultRequestDTO();
    dto.setExamId("exam-123");
    dto.setUserId(student.getId());

    Exercise exercise = Exercise.builder().id("exercise-1").title("Exercise 1").code("EX1").build();

    // Auto-submitted exercise (no submissionId)
    ExamSubmission examSubmission = ExamSubmission.builder()
      .id("exam-sub-1")
      .exercise(exercise)
      .submissionId(null)
      .score(0.0)
      .build();

    when(examRepository.findById("exam-123")).thenReturn(Optional.of(exam));
    when(usersRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(examSubmissionRepository.findAll(any(Specification.class))).thenReturn(
      List.of(examSubmission)
    );

    // When
    ExamResultResponseDTO result = examSubmissionService.getExamResult(dto, student);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getSubmissions().size());
    ExamResultResponseDTO.ExamSubmissionDetail detail = result.getSubmissions().get(0);
    assertNull(detail.getSubmissionId());
    assertEquals(0.0, detail.getScore());
    assertFalse(detail.getIsAccepted());
  }
}
