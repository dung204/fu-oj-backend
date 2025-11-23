package com.example.modules.exams.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.base.BaseServiceTest;
import com.example.modules.auth.enums.Role;
import com.example.modules.exams.dtos.ExamCreateDTO;
import com.example.modules.exams.dtos.ExamResponseDTO;
import com.example.modules.exams.dtos.ExamUpdateDTO;
import com.example.modules.exams.dtos.ExamsSearchDTO;
import com.example.modules.exams.entities.Exam;
import com.example.modules.exams.entities.ExamSubmission;
import com.example.modules.exams.entities.GroupExam;
import com.example.modules.exams.enums.ExamStatus;
import com.example.modules.exams.exceptions.ExamNotFoundException;
import com.example.modules.exams.exceptions.ExamNotModifiableException;
import com.example.modules.exams.exceptions.InvalidTimeRangeException;
import com.example.modules.exams.exceptions.StartTimeTooSoonException;
import com.example.modules.exams.repositories.ExamExerciseRepository;
import com.example.modules.exams.repositories.ExamRepository;
import com.example.modules.exams.repositories.ExamSubmissionRepository;
import com.example.modules.exams.repositories.GroupExamRepository;
import com.example.modules.exams.utils.ExamMapper;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.groups.entities.Group;
import com.example.modules.groups.repositories.GroupsRepository;
import com.example.modules.redis.services.RedisService;
import com.example.modules.users.entities.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@SuppressWarnings("unchecked")
public class ExamServiceTest extends BaseServiceTest {

  @Mock
  private ExamRepository examRepository;

  @Mock
  private ExamExerciseRepository examExerciseRepository;

  @Mock
  private ExamSubmissionRepository examSubmissionRepository;

  @Mock
  private GroupExamRepository groupExamRepository;

  @Mock
  private GroupsRepository groupsRepository;

  @Mock
  private ExercisesRepository exercisesRepository;

  @Mock
  private ExamMapper examMapper;

  @Mock
  private RedisService redisService;

  @InjectMocks
  private ExamService examService;

  private Exam getMockExam() {
    Instant now = Instant.now();
    return Exam.builder()
      .id("exam-123")
      .code("EXAM-ABC123")
      .title("Test Exam")
      .description("Test Description")
      .startTime(now.plus(10, ChronoUnit.MINUTES))
      .endTime(now.plus(100, ChronoUnit.MINUTES))
      .timeLimit(90.0)
      .build();
  }

  private User getMockUserWithRole(Role role) {
    User user = getMockUser();
    user.getAccount().setRole(role);
    return user;
  }

  @Test
  void createExamForMultipleGroups_WhenValidData_ShouldCreateExam() {
    // Given
    Instant now = Instant.now();
    ExamCreateDTO dto = new ExamCreateDTO();
    dto.setTitle("Test Exam");
    dto.setDescription("Description");
    dto.setStartTime(now.plus(10, ChronoUnit.MINUTES));
    dto.setEndTime(now.plus(100, ChronoUnit.MINUTES));
    dto.setTimeLimit(90.0);
    dto.setStatus(ExamStatus.UPCOMING.getValue());
    dto.setGroupIds(List.of("group-1", "group-2"));
    dto.setExerciseIds(List.of("exercise-1", "exercise-2"));

    Group group1 = Group.builder().id("group-1").build();
    Group group2 = Group.builder().id("group-2").build();
    Exercise exercise1 = Exercise.builder().id("exercise-1").build();
    Exercise exercise2 = Exercise.builder().id("exercise-2").build();

    Exam savedExam = getMockExam();
    ExamResponseDTO responseDTO = ExamResponseDTO.builder().id(savedExam.getId()).build();

    when(groupsRepository.findAllById(dto.getGroupIds())).thenReturn(List.of(group1, group2));
    when(exercisesRepository.findAllById(dto.getExerciseIds())).thenReturn(
      List.of(exercise1, exercise2)
    );
    when(examRepository.save(any(Exam.class))).thenReturn(savedExam);
    when(examMapper.toExamResponseDTO(savedExam)).thenReturn(responseDTO);
    doNothing().when(redisService).set(anyString(), any(), any());

    // When
    ExamResponseDTO result = examService.createExamForMultipleGroups(dto);

    // Then
    assertNotNull(result);
    verify(examRepository).save(any(Exam.class));
    verify(examExerciseRepository).saveAll(anyList());
    verify(groupExamRepository).saveAll(anyList());
  }

  @Test
  void createExamForMultipleGroups_WhenStartTimeAfterEndTime_ShouldThrowException() {
    // Given
    Instant now = Instant.now();
    ExamCreateDTO dto = new ExamCreateDTO();
    dto.setStartTime(now.plus(100, ChronoUnit.MINUTES));
    dto.setEndTime(now.plus(10, ChronoUnit.MINUTES)); // End before start
    dto.setTimeLimit(90.0);
    dto.setStatus(ExamStatus.UPCOMING.getValue());

    // When & Then
    assertThrows(InvalidTimeRangeException.class, () ->
      examService.createExamForMultipleGroups(dto)
    );
    verify(examRepository, never()).save(any(Exam.class));
  }

  @Test
  void createExamForMultipleGroups_WhenStartTimeTooSoon_ShouldThrowException() {
    // Given
    Instant now = Instant.now();
    ExamCreateDTO dto = new ExamCreateDTO();
    dto.setTitle("Test Exam");
    dto.setDescription("Description");
    dto.setStartTime(now.plus(2, ChronoUnit.MINUTES)); // Too soon (< 5 minutes)
    dto.setEndTime(now.plus(100, ChronoUnit.MINUTES));
    dto.setTimeLimit(90.0);
    dto.setStatus(ExamStatus.UPCOMING.getValue());

    // When & Then
    assertThrows(StartTimeTooSoonException.class, () ->
      examService.createExamForMultipleGroups(dto)
    );
    verify(examRepository, never()).save(any(Exam.class));
  }

  @Test
  void getExamById_WhenExamExists_ShouldReturnExam() {
    // Given
    User student = getMockUserWithRole(Role.STUDENT);
    Exam exam = getMockExam();

    when(examRepository.findOne(any(Specification.class))).thenReturn(Optional.of(exam));

    // When
    Exam result = examService.getExamById("exam-123", student);

    // Then
    assertNotNull(result);
    assertEquals("exam-123", result.getId());
    verify(examRepository).findOne(any(Specification.class));
  }

  @Test
  void getExamById_WhenExamNotFound_ShouldThrowException() {
    // Given
    User student = getMockUserWithRole(Role.STUDENT);
    when(examRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ExamNotFoundException.class, () ->
      examService.getExamById("non-existent", student)
    );
  }

  @Test
  void getAllExams_WhenStudentRole_ShouldReturnOnlyGroupExams() {
    // Given
    User student = getMockUserWithRole(Role.STUDENT);
    student.setJoinedGroups(Set.of(Group.builder().id("group-1").build()));

    ExamsSearchDTO searchDTO = new ExamsSearchDTO();
    searchDTO.setPage(1);
    searchDTO.setPageSize(10);

    Exam exam = getMockExam();
    Page<Exam> examPage = new PageImpl<>(List.of(exam), PageRequest.of(0, 10), 1);
    ExamResponseDTO responseDTO = ExamResponseDTO.builder().id(exam.getId()).build();

    when(examRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(
      examPage
    );
    when(examMapper.toExamResponseDTO(exam)).thenReturn(responseDTO);

    // When
    Page<ExamResponseDTO> result = examService.getAllExams(searchDTO, student);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(examRepository).findAll(any(Specification.class), any(PageRequest.class));
  }

  @Test
  void updateExam_WhenExamAlreadyStarted_ShouldThrowException() {
    // Given
    User instructor = getMockUserWithRole(Role.INSTRUCTOR);
    Exam exam = getMockExam();
    exam.setStartTime(Instant.now().minus(10, ChronoUnit.MINUTES)); // Already started

    ExamUpdateDTO updateDTO = new ExamUpdateDTO();

    when(examRepository.findOne(any(Specification.class))).thenReturn(Optional.of(exam));

    // When & Then
    assertThrows(ExamNotModifiableException.class, () ->
      examService.updateExam("exam-123", updateDTO, instructor)
    );
    verify(examRepository, never()).save(any(Exam.class));
  }

  @Test
  void updateExam_WhenHasSubmissions_ShouldThrowException() {
    // Given
    User instructor = getMockUserWithRole(Role.INSTRUCTOR);
    Exam exam = getMockExam();
    ExamUpdateDTO updateDTO = new ExamUpdateDTO();

    ExamSubmission submission = ExamSubmission.builder().id("sub-1").build();

    when(examRepository.findOne(any(Specification.class))).thenReturn(Optional.of(exam));
    when(examSubmissionRepository.findByExamId(exam.getId())).thenReturn(List.of(submission));

    // When & Then
    assertThrows(ExamNotModifiableException.class, () ->
      examService.updateExam("exam-123", updateDTO, instructor)
    );
    verify(examRepository, never()).save(any(Exam.class));
  }

  @Test
  void publishExam_WhenAllGroupsDraft_ShouldPublishSuccessfully() {
    // Given
    User instructor = getMockUserWithRole(Role.INSTRUCTOR);
    Instant now = Instant.now();
    Exam exam = getMockExam();
    exam.setStartTime(now.plus(10, ChronoUnit.MINUTES));
    exam.setEndTime(now.plus(100, ChronoUnit.MINUTES));

    GroupExam groupExam1 = GroupExam.builder()
      .id("ge-1")
      .exam(exam)
      .status(ExamStatus.DRAFT)
      .build();
    GroupExam groupExam2 = GroupExam.builder()
      .id("ge-2")
      .exam(exam)
      .status(ExamStatus.DRAFT)
      .build();

    ExamResponseDTO responseDTO = ExamResponseDTO.builder().id(exam.getId()).build();

    when(examRepository.findOne(any(Specification.class))).thenReturn(Optional.of(exam));
    when(groupExamRepository.findByExamId(exam.getId())).thenReturn(
      List.of(groupExam1, groupExam2)
    );
    when(examRepository.save(exam)).thenReturn(exam);
    when(examMapper.toExamResponseDTO(exam)).thenReturn(responseDTO);

    // When
    ExamResponseDTO result = examService.publishExam("exam-123", instructor);

    // Then
    assertNotNull(result);
    verify(groupExamRepository).saveAll(anyList());
    verify(examRepository).save(exam);
  }

  @Test
  void deleteExam_WhenValidExam_ShouldSoftDelete() {
    // Given
    User instructor = getMockUserWithRole(Role.INSTRUCTOR);
    Exam exam = getMockExam();
    ExamResponseDTO responseDTO = ExamResponseDTO.builder().id(exam.getId()).build();

    when(examRepository.findOne(any(Specification.class))).thenReturn(Optional.of(exam));
    when(examRepository.save(exam)).thenReturn(exam);
    when(examMapper.toExamResponseDTO(exam)).thenReturn(responseDTO);

    // When
    ExamResponseDTO result = examService.deleteExam("exam-123", instructor);

    // Then
    assertNotNull(result);
    verify(examRepository).save(exam);
  }

  @Test
  void generateUniqueExamCode_ShouldReturnUniqueCode() {
    // Given
    when(examRepository.existsByCode(anyString())).thenReturn(false);

    // When
    String code = examService.generateUniqueExamCode();

    // Then
    assertNotNull(code);
    assertTrue(code.startsWith("EXAM-"));
    assertEquals(11, code.length()); // EXAM- + 6 chars
  }
}
