package com.example.modules.exercises.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.base.BaseServiceTest;
import com.example.modules.auth.enums.Role;
import com.example.modules.exercises.dtos.ExerciseQueryDTO;
import com.example.modules.exercises.dtos.ExerciseRequestDTO;
import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.enums.Difficulty;
import com.example.modules.exercises.enums.Visibility;
import com.example.modules.exercises.exceptions.ExerciseNotFoundException;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.exercises.utils.ExerciseMapper;
import com.example.modules.groups.entities.Group;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.test_cases.dtos.TestCaseRequestDTO;
import com.example.modules.test_cases.entities.TestCase;
import com.example.modules.test_cases.repositories.TestCasesRepository;
import com.example.modules.topics.entities.Topic;
import com.example.modules.topics.repositories.TopicsRepository;
import com.example.modules.users.entities.User;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
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
public class ExercisesServiceTest extends BaseServiceTest {

  @Mock
  private ExercisesRepository exercisesRepository;

  @Mock
  private TopicsRepository topicsRepository;

  @Mock
  private TestCasesRepository testCasesRepository;

  @Mock
  private ExerciseMapper exerciseMapper;

  @Mock
  private SubmissionRepository submissionRepository;

  @InjectMocks
  private ExercisesService exercisesService;

  private Exercise getMockExercise() {
    return Exercise.builder()
      .id("exercise-123")
      .code("EX001")
      .title("Test Exercise")
      .description("Test Description")
      .difficulty(Difficulty.EASY)
      .visibility(Visibility.PUBLIC)
      .timeLimit(1.0)
      .memory(65536.0)
      .maxSubmissions(10)
      .version(1)
      .baseId("exercise-123")
      .createdTimestamp(Instant.now())
      .updatedTimestamp(Instant.now())
      .build();
  }

  private User getMockUserWithRole(Role role) {
    User user = getMockUser();
    user.getAccount().setRole(role);
    return user;
  }

  // ============== getExerciseById Tests (5 test cases) ==============

  @Test
  void getExerciseById_WhenAdmin_ShouldReturnExercise() {
    User adminUser = getMockUserWithRole(Role.ADMIN);
    Exercise mockExercise = getMockExercise();

    when(exercisesRepository.findOne(any(Specification.class))).thenReturn(
      Optional.of(mockExercise)
    );

    Exercise result = exercisesService.getExerciseById("exercise-123", adminUser);

    assertNotNull(result);
    assertEquals("exercise-123", result.getId());
    verify(exercisesRepository).findOne(any(Specification.class));
  }

  @Test
  void getExerciseById_WhenInstructorAccessOwnExercise_ShouldReturnExercise() {
    User instructorUser = getMockUserWithRole(Role.INSTRUCTOR);
    Exercise mockExercise = getMockExercise();
    mockExercise.setCreatedBy(instructorUser.getId());

    when(exercisesRepository.findOne(any(Specification.class))).thenReturn(
      Optional.of(mockExercise)
    );

    Exercise result = exercisesService.getExerciseById("exercise-123", instructorUser);

    assertNotNull(result);
    assertEquals("exercise-123", result.getId());
  }

  @Test
  void getExerciseById_WhenStudentAccessPublicExercise_ShouldReturnExercise() {
    User studentUser = getMockUserWithRole(Role.STUDENT);
    studentUser.setJoinedGroups(Set.of());
    Exercise mockExercise = getMockExercise();
    mockExercise.setVisibility(Visibility.PUBLIC);

    when(exercisesRepository.findOne(any(Specification.class))).thenReturn(
      Optional.of(mockExercise)
    );

    Exercise result = exercisesService.getExerciseById("exercise-123", studentUser);

    assertNotNull(result);
    assertEquals(Visibility.PUBLIC, result.getVisibility());
  }

  @Test
  void getExerciseById_WhenStudentAccessPrivateExerciseInGroup_ShouldReturnExercise() {
    User studentUser = getMockUserWithRole(Role.STUDENT);
    Group group = Group.builder().id("group-1").build();
    studentUser.setJoinedGroups(Set.of(group));

    Exercise mockExercise = getMockExercise();
    mockExercise.setVisibility(Visibility.PRIVATE);
    mockExercise.setGroups(List.of(group));

    when(exercisesRepository.findOne(any(Specification.class))).thenReturn(
      Optional.of(mockExercise)
    );

    Exercise result = exercisesService.getExerciseById("exercise-123", studentUser);

    assertNotNull(result);
    assertEquals(Visibility.PRIVATE, result.getVisibility());
  }

  @Test
  void getExerciseById_WhenExerciseNotFound_ShouldThrowException() {
    User adminUser = getMockUserWithRole(Role.ADMIN);

    when(exercisesRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

    assertThrows(ExerciseNotFoundException.class, () ->
      exercisesService.getExerciseById("non-existent", adminUser)
    );
  }

  // ============== createExercise Tests (5 test cases) ==============

  @Test
  void createExercise_ShouldCreateAndReturnExerciseResponseDTO() {
    ExerciseRequestDTO request = new ExerciseRequestDTO();
    request.setCode("EX001");
    request.setTitle("New Exercise");
    request.setDescription("New Description");
    request.setDifficulty("EASY");
    request.setVisibility("PUBLIC");
    request.setTimeLimit(1.0);
    request.setMemory(65536.0);
    request.setMaxSubmissions(10);

    Exercise savedExercise = getMockExercise();
    ExerciseResponseDTO responseDTO = ExerciseResponseDTO.builder()
      .id(savedExercise.getId())
      .code(savedExercise.getCode())
      .title(savedExercise.getTitle())
      .build();

    when(exercisesRepository.exists(any(Specification.class))).thenReturn(false);
    when(exercisesRepository.save(any(Exercise.class))).thenReturn(savedExercise);
    when(exerciseMapper.toExerciseResponseDTOWithAllTestCases(savedExercise)).thenReturn(
      responseDTO
    );

    ExerciseResponseDTO result = exercisesService.createExercise(request);

    assertNotNull(result);
    assertEquals("EX001", result.getCode());
    verify(exercisesRepository, times(2)).save(any(Exercise.class)); // save 2 times: initial + set baseId
  }

  @Test
  void createExercise_WhenCodeExists_ShouldThrowException() {
    ExerciseRequestDTO request = new ExerciseRequestDTO();
    request.setCode("EX001");
    request.setTitle("New Exercise");
    request.setDescription("New Description");
    request.setDifficulty("EASY");
    request.setVisibility("PUBLIC");

    when(exercisesRepository.exists(any(Specification.class))).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () -> exercisesService.createExercise(request));
    verify(exercisesRepository, never()).save(any(Exercise.class));
  }

  @Test
  void createExercise_WithTopics_ShouldCreateExerciseWithTopics() {
    ExerciseRequestDTO request = new ExerciseRequestDTO();
    request.setCode("EX001");
    request.setTitle("New Exercise");
    request.setDescription("New Description");
    request.setDifficulty("EASY");
    request.setVisibility("PUBLIC");
    request.setTopicIds(List.of("topic-1", "topic-2"));

    Topic topic1 = Topic.builder().id("topic-1").build();
    Topic topic2 = Topic.builder().id("topic-2").build();

    Exercise savedExercise = getMockExercise();
    savedExercise.setTopics(List.of(topic1, topic2));

    ExerciseResponseDTO responseDTO = ExerciseResponseDTO.builder()
      .id(savedExercise.getId())
      .code(savedExercise.getCode())
      .build();

    when(exercisesRepository.exists(any(Specification.class))).thenReturn(false);
    when(topicsRepository.findAllById(List.of("topic-1", "topic-2"))).thenReturn(
      List.of(topic1, topic2)
    );
    when(exercisesRepository.save(any(Exercise.class))).thenReturn(savedExercise);
    when(exerciseMapper.toExerciseResponseDTOWithAllTestCases(savedExercise)).thenReturn(
      responseDTO
    );

    ExerciseResponseDTO result = exercisesService.createExercise(request);

    assertNotNull(result);
    verify(topicsRepository).findAllById(List.of("topic-1", "topic-2"));
  }

  @Test
  void createExercise_WithInvalidTopicIds_ShouldThrowException() {
    ExerciseRequestDTO request = new ExerciseRequestDTO();
    request.setCode("EX001");
    request.setTitle("New Exercise");
    request.setDescription("New Description");
    request.setDifficulty("EASY");
    request.setVisibility("PUBLIC");
    request.setTopicIds(List.of("topic-1", "topic-2"));

    when(exercisesRepository.exists(any(Specification.class))).thenReturn(false);
    when(topicsRepository.findAllById(List.of("topic-1", "topic-2"))).thenReturn(
      List.of(Topic.builder().id("topic-1").build())
    );

    assertThrows(EntityNotFoundException.class, () -> exercisesService.createExercise(request));
  }

  @Test
  void createExercise_WithTestCases_ShouldCreateExerciseWithTestCases() {
    ExerciseRequestDTO request = new ExerciseRequestDTO();
    request.setCode("EX001");
    request.setTitle("New Exercise");
    request.setDescription("New Description");
    request.setDifficulty("EASY");
    request.setVisibility("PUBLIC");

    TestCaseRequestDTO testCaseRequest = new TestCaseRequestDTO();
    testCaseRequest.setInput("1 2");
    testCaseRequest.setOutput("3");
    testCaseRequest.setIsPublic(true);
    request.setTestCases(List.of(testCaseRequest));

    Exercise savedExercise = getMockExercise();
    TestCase testCase = TestCase.builder()
      .id("testcase-1")
      .exercise(savedExercise)
      .input("1 2")
      .output("3")
      .isPublic(true)
      .build();
    savedExercise.setTestCases(List.of(testCase));

    ExerciseResponseDTO responseDTO = ExerciseResponseDTO.builder()
      .id(savedExercise.getId())
      .code(savedExercise.getCode())
      .build();

    when(exercisesRepository.exists(any(Specification.class))).thenReturn(false);
    when(exercisesRepository.save(any(Exercise.class))).thenReturn(savedExercise);
    when(testCasesRepository.saveAll(any(List.class))).thenReturn(List.of(testCase));
    when(exerciseMapper.toExerciseResponseDTOWithAllTestCases(savedExercise)).thenReturn(
      responseDTO
    );

    ExerciseResponseDTO result = exercisesService.createExercise(request);

    assertNotNull(result);
    verify(testCasesRepository).saveAll(any(List.class));
  }

  // ============== getExercises Tests (5 test cases) ==============

  @Test
  void getExercises_WhenAdmin_ShouldReturnAllExercises() {
    User adminUser = getMockUserWithRole(Role.ADMIN);
    ExerciseQueryDTO queryDTO = new ExerciseQueryDTO();
    queryDTO.setPage(1);
    queryDTO.setPageSize(10);

    Exercise exercise = getMockExercise();
    Page<Exercise> exercisePage = new PageImpl<>(List.of(exercise), PageRequest.of(0, 10), 1);
    ExerciseResponseDTO responseDTO = ExerciseResponseDTO.builder().id(exercise.getId()).build();

    when(exercisesRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(
      exercisePage
    );
    when(exerciseMapper.toExerciseResponseDTOWithAllTestCases(exercise)).thenReturn(responseDTO);

    Page<ExerciseResponseDTO> result = exercisesService.getExercises(queryDTO, adminUser);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(exercisesRepository).findAll(any(Specification.class), any(PageRequest.class));
  }

  @Test
  void getExercises_WhenInstructor_ShouldReturnPublicAndOwnExercises() {
    User instructorUser = getMockUserWithRole(Role.INSTRUCTOR);
    ExerciseQueryDTO queryDTO = new ExerciseQueryDTO();
    queryDTO.setPage(1);
    queryDTO.setPageSize(10);

    Exercise exercise = getMockExercise();
    exercise.setCreatedBy(instructorUser.getId());
    Page<Exercise> exercisePage = new PageImpl<>(List.of(exercise), PageRequest.of(0, 10), 1);
    ExerciseResponseDTO responseDTO = ExerciseResponseDTO.builder().id(exercise.getId()).build();

    when(exercisesRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(
      exercisePage
    );
    when(exerciseMapper.toExerciseResponseDTOWithAllTestCases(exercise)).thenReturn(responseDTO);

    Page<ExerciseResponseDTO> result = exercisesService.getExercises(queryDTO, instructorUser);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
  }

  @Test
  void getExercises_WhenStudent_ShouldHidePrivateTestCases() {
    User studentUser = getMockUserWithRole(Role.STUDENT);
    studentUser.setJoinedGroups(Set.of());
    ExerciseQueryDTO queryDTO = new ExerciseQueryDTO();
    queryDTO.setPage(1);
    queryDTO.setPageSize(10);

    Exercise exercise = getMockExercise();
    Page<Exercise> exercisePage = new PageImpl<>(List.of(exercise), PageRequest.of(0, 10), 1);
    ExerciseResponseDTO responseDTO = ExerciseResponseDTO.builder().id(exercise.getId()).build();

    when(exercisesRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(
      exercisePage
    );
    when(exerciseMapper.toExerciseResponseDTOWithPrivateTestCasesHidden(exercise)).thenReturn(
      responseDTO
    );

    Page<ExerciseResponseDTO> result = exercisesService.getExercises(queryDTO, studentUser);

    assertNotNull(result);
    verify(exerciseMapper).toExerciseResponseDTOWithPrivateTestCasesHidden(exercise);
  }

  @Test
  void getExercises_WithQueryFilter_ShouldReturnFilteredResults() {
    User adminUser = getMockUserWithRole(Role.ADMIN);
    ExerciseQueryDTO queryDTO = new ExerciseQueryDTO();
    queryDTO.setQuery("Array");
    queryDTO.setPage(1);
    queryDTO.setPageSize(10);

    Exercise exercise = getMockExercise();
    exercise.setTitle("Array Problems");
    Page<Exercise> exercisePage = new PageImpl<>(List.of(exercise), PageRequest.of(0, 10), 1);
    ExerciseResponseDTO responseDTO = ExerciseResponseDTO.builder()
      .id(exercise.getId())
      .title("Array Problems")
      .build();

    when(exercisesRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(
      exercisePage
    );
    when(exerciseMapper.toExerciseResponseDTOWithAllTestCases(exercise)).thenReturn(responseDTO);

    Page<ExerciseResponseDTO> result = exercisesService.getExercises(queryDTO, adminUser);

    assertNotNull(result);
    assertEquals("Array Problems", result.getContent().get(0).getTitle());
  }

  @Test
  void getExercises_WithTopicFilter_ShouldReturnExercisesWithTopic() {
    User adminUser = getMockUserWithRole(Role.ADMIN);
    ExerciseQueryDTO queryDTO = new ExerciseQueryDTO();
    queryDTO.setTopic(List.of("topic-1"));
    queryDTO.setPage(1);
    queryDTO.setPageSize(10);

    Exercise exercise = getMockExercise();
    Topic topic = Topic.builder().id("topic-1").name("Algorithms").build();
    exercise.setTopics(List.of(topic));
    Page<Exercise> exercisePage = new PageImpl<>(List.of(exercise), PageRequest.of(0, 10), 1);
    ExerciseResponseDTO responseDTO = ExerciseResponseDTO.builder().id(exercise.getId()).build();

    when(exercisesRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(
      exercisePage
    );
    when(exerciseMapper.toExerciseResponseDTOWithAllTestCases(exercise)).thenReturn(responseDTO);

    Page<ExerciseResponseDTO> result = exercisesService.getExercises(queryDTO, adminUser);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
  }

  // ============== getExercisesByCourseId Tests (5 test cases) ==============

  @Test
  void getExercisesByCourseId_WhenCourseHasExercises_ShouldReturnExercises() {
    User adminUser = getMockUserWithRole(Role.ADMIN);
    ExerciseQueryDTO queryDTO = new ExerciseQueryDTO();
    queryDTO.setPage(1);
    queryDTO.setPageSize(10);

    Exercise exercise = getMockExercise();
    Page<Exercise> exercisePage = new PageImpl<>(List.of(exercise), PageRequest.of(0, 10), 1);
    ExerciseResponseDTO responseDTO = ExerciseResponseDTO.builder().id(exercise.getId()).build();

    when(exercisesRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(
      exercisePage
    );
    when(exerciseMapper.toExerciseResponseDTOWithAllTestCases(exercise)).thenReturn(responseDTO);

    Page<ExerciseResponseDTO> result = exercisesService.getExercisesByCourseId(
      "course-1",
      queryDTO,
      adminUser
    );

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
  }

  @Test
  void getExercisesByCourseId_WhenStudentAccess_ShouldHidePrivateTestCases() {
    User studentUser = getMockUserWithRole(Role.STUDENT);
    studentUser.setJoinedGroups(Set.of());
    ExerciseQueryDTO queryDTO = new ExerciseQueryDTO();
    queryDTO.setPage(1);
    queryDTO.setPageSize(10);

    Exercise exercise = getMockExercise();
    Page<Exercise> exercisePage = new PageImpl<>(List.of(exercise), PageRequest.of(0, 10), 1);
    ExerciseResponseDTO responseDTO = ExerciseResponseDTO.builder().id(exercise.getId()).build();

    when(exercisesRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(
      exercisePage
    );
    when(exerciseMapper.toExerciseResponseDTOWithPrivateTestCasesHidden(exercise)).thenReturn(
      responseDTO
    );

    Page<ExerciseResponseDTO> result = exercisesService.getExercisesByCourseId(
      "course-1",
      queryDTO,
      studentUser
    );

    assertNotNull(result);
    verify(exerciseMapper).toExerciseResponseDTOWithPrivateTestCasesHidden(exercise);
  }

  @Test
  void getExercisesByCourseId_WhenCourseEmpty_ShouldReturnEmptyPage() {
    User adminUser = getMockUserWithRole(Role.ADMIN);
    ExerciseQueryDTO queryDTO = new ExerciseQueryDTO();
    queryDTO.setPage(1);
    queryDTO.setPageSize(10);

    Page<Exercise> exercisePage = new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 10), 0);

    when(exercisesRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(
      exercisePage
    );

    Page<ExerciseResponseDTO> result = exercisesService.getExercisesByCourseId(
      "course-empty",
      queryDTO,
      adminUser
    );

    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
  }

  @Test
  void getExercisesByCourseId_WithQueryFilter_ShouldReturnFilteredExercises() {
    User adminUser = getMockUserWithRole(Role.ADMIN);
    ExerciseQueryDTO queryDTO = new ExerciseQueryDTO();
    queryDTO.setQuery("Binary");
    queryDTO.setPage(1);
    queryDTO.setPageSize(10);

    Exercise exercise = getMockExercise();
    exercise.setTitle("Binary Search");
    Page<Exercise> exercisePage = new PageImpl<>(List.of(exercise), PageRequest.of(0, 10), 1);
    ExerciseResponseDTO responseDTO = ExerciseResponseDTO.builder()
      .id(exercise.getId())
      .title("Binary Search")
      .build();

    when(exercisesRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(
      exercisePage
    );
    when(exerciseMapper.toExerciseResponseDTOWithAllTestCases(exercise)).thenReturn(responseDTO);

    Page<ExerciseResponseDTO> result = exercisesService.getExercisesByCourseId(
      "course-1",
      queryDTO,
      adminUser
    );

    assertNotNull(result);
    assertEquals("Binary Search", result.getContent().get(0).getTitle());
  }

  @Test
  void getExercisesByCourseId_OnlyLatestVersion_ShouldReturnLatestVersionOnly() {
    User adminUser = getMockUserWithRole(Role.ADMIN);
    ExerciseQueryDTO queryDTO = new ExerciseQueryDTO();
    queryDTO.setPage(1);
    queryDTO.setPageSize(10);

    Exercise exercise = getMockExercise();
    exercise.setVersion(3); // Latest version
    Page<Exercise> exercisePage = new PageImpl<>(List.of(exercise), PageRequest.of(0, 10), 1);
    ExerciseResponseDTO responseDTO = ExerciseResponseDTO.builder()
      .id(exercise.getId())
      .version(3)
      .build();

    when(exercisesRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(
      exercisePage
    );
    when(exerciseMapper.toExerciseResponseDTOWithAllTestCases(exercise)).thenReturn(responseDTO);

    Page<ExerciseResponseDTO> result = exercisesService.getExercisesByCourseId(
      "course-1",
      queryDTO,
      adminUser
    );

    assertNotNull(result);
    assertEquals(3, result.getContent().get(0).getVersion());
  }

  // ============== updateExercise Tests (5 test cases) ==============

  @Test
  void updateExercise_WhenDraftAndNoSubmissions_ShouldUpdateInPlace() {
    User instructorUser = getMockUserWithRole(Role.INSTRUCTOR);
    Exercise existingExercise = getMockExercise();
    existingExercise.setVisibility(Visibility.DRAFT);
    existingExercise.setCreatedBy(instructorUser.getId());

    ExerciseRequestDTO request = new ExerciseRequestDTO();
    request.setCode("EX001");
    request.setTitle("Updated Title");
    request.setDescription("Updated Description");
    request.setDifficulty("MEDIUM");
    request.setVisibility("PUBLIC");

    ExerciseResponseDTO responseDTO = ExerciseResponseDTO.builder()
      .id(existingExercise.getId())
      .code("EX001")
      .title("Updated Title")
      .build();

    when(exercisesRepository.findOne(any(Specification.class))).thenReturn(
      Optional.of(existingExercise)
    );
    when(submissionRepository.getSubmissionByExercise(existingExercise)).thenReturn(null);
    when(exercisesRepository.save(existingExercise)).thenReturn(existingExercise);
    when(exerciseMapper.toExerciseResponseDTOWithAllTestCases(existingExercise)).thenReturn(
      responseDTO
    );

    ExerciseResponseDTO result = exercisesService.updateExercise(
      "exercise-123",
      request,
      instructorUser
    );

    assertNotNull(result);
    assertEquals("Updated Title", result.getTitle());
    verify(exercisesRepository).save(existingExercise);
  }

  @Test
  void updateExercise_WhenHasSubmissions_ShouldCreateNewVersion() {
    User instructorUser = getMockUserWithRole(Role.INSTRUCTOR);
    Exercise existingExercise = getMockExercise();
    existingExercise.setVisibility(Visibility.PUBLIC);
    existingExercise.setCreatedBy(instructorUser.getId());

    Submission submission = Submission.builder().id("submission-1").build();

    ExerciseRequestDTO request = new ExerciseRequestDTO();
    request.setCode("EX001");
    request.setTitle("Updated Title");
    request.setDescription("Updated Description");
    request.setDifficulty("MEDIUM");
    request.setVisibility("PUBLIC");

    Exercise newVersion = getMockExercise();
    newVersion.setId("exercise-456");
    newVersion.setVersion(2);

    ExerciseResponseDTO responseDTO = ExerciseResponseDTO.builder()
      .id(newVersion.getId())
      .code("EX001")
      .title("Updated Title")
      .version(2)
      .build();

    when(exercisesRepository.findOne(any(Specification.class))).thenReturn(
      Optional.of(existingExercise)
    );
    when(submissionRepository.getSubmissionByExercise(existingExercise)).thenReturn(submission);
    when(exercisesRepository.save(any(Exercise.class))).thenReturn(newVersion);
    when(testCasesRepository.findAllByExerciseId(existingExercise.getId())).thenReturn(
      new ArrayList<>()
    );
    when(exerciseMapper.toExerciseResponseDTOWithAllTestCases(newVersion)).thenReturn(responseDTO);

    ExerciseResponseDTO result = exercisesService.updateExercise(
      "exercise-123",
      request,
      instructorUser
    );

    assertNotNull(result);
    assertEquals(2, result.getVersion());
    verify(exercisesRepository).save(any(Exercise.class));
  }

  @Test
  void updateExercise_WhenChangingCodeToDuplicateCode_ShouldThrowException() {
    User instructorUser = getMockUserWithRole(Role.INSTRUCTOR);
    Exercise existingExercise = getMockExercise();
    existingExercise.setCode("EX001");
    existingExercise.setVisibility(Visibility.DRAFT);
    existingExercise.setCreatedBy(instructorUser.getId());

    ExerciseRequestDTO request = new ExerciseRequestDTO();
    request.setCode("EX002"); // Different code that already exists

    when(exercisesRepository.findOne(any(Specification.class))).thenReturn(
      Optional.of(existingExercise)
    );
    when(submissionRepository.getSubmissionByExercise(existingExercise)).thenReturn(null);
    when(exercisesRepository.existsByCode("EX002")).thenReturn(true);

    assertThrows(IllegalArgumentException.class, () ->
      exercisesService.updateExercise("exercise-123", request, instructorUser)
    );
  }

  @Test
  void updateExercise_WithNewTopics_ShouldUpdateTopics() {
    User instructorUser = getMockUserWithRole(Role.INSTRUCTOR);
    Exercise existingExercise = getMockExercise();
    existingExercise.setVisibility(Visibility.DRAFT);
    existingExercise.setCreatedBy(instructorUser.getId());

    Topic topic1 = Topic.builder().id("topic-1").build();
    Topic topic2 = Topic.builder().id("topic-2").build();

    ExerciseRequestDTO request = new ExerciseRequestDTO();
    request.setCode("EX001");
    request.setTitle("Updated Exercise");
    request.setDifficulty("EASY");
    request.setVisibility("DRAFT");
    request.setTopicIds(List.of("topic-1", "topic-2"));

    ExerciseResponseDTO responseDTO = ExerciseResponseDTO.builder()
      .id(existingExercise.getId())
      .build();

    when(exercisesRepository.findOne(any(Specification.class))).thenReturn(
      Optional.of(existingExercise)
    );
    when(submissionRepository.getSubmissionByExercise(existingExercise)).thenReturn(null);
    when(topicsRepository.findAllById(request.getTopicIds())).thenReturn(List.of(topic1, topic2));
    when(exercisesRepository.save(existingExercise)).thenReturn(existingExercise);
    when(exerciseMapper.toExerciseResponseDTOWithAllTestCases(existingExercise)).thenReturn(
      responseDTO
    );

    ExerciseResponseDTO result = exercisesService.updateExercise(
      "exercise-123",
      request,
      instructorUser
    );

    assertNotNull(result);
    verify(topicsRepository).findAllById(request.getTopicIds());
  }

  @Test
  void updateExercise_WithEmptyTopics_ShouldClearTopics() {
    User instructorUser = getMockUserWithRole(Role.INSTRUCTOR);
    Exercise existingExercise = getMockExercise();
    existingExercise.setVisibility(Visibility.DRAFT);
    existingExercise.setCreatedBy(instructorUser.getId());
    existingExercise.setTopics(List.of(Topic.builder().id("topic-1").build()));

    ExerciseRequestDTO request = new ExerciseRequestDTO();
    request.setCode("EX001");
    request.setTitle("Updated Exercise");
    request.setDifficulty("EASY");
    request.setVisibility("DRAFT");
    request.setTopicIds(new ArrayList<>()); // Clear topics

    ExerciseResponseDTO responseDTO = ExerciseResponseDTO.builder()
      .id(existingExercise.getId())
      .build();

    when(exercisesRepository.findOne(any(Specification.class))).thenReturn(
      Optional.of(existingExercise)
    );
    when(submissionRepository.getSubmissionByExercise(existingExercise)).thenReturn(null);
    when(exercisesRepository.save(existingExercise)).thenReturn(existingExercise);
    when(exerciseMapper.toExerciseResponseDTOWithAllTestCases(existingExercise)).thenReturn(
      responseDTO
    );

    ExerciseResponseDTO result = exercisesService.updateExercise(
      "exercise-123",
      request,
      instructorUser
    );

    assertNotNull(result);
    verify(exercisesRepository).save(existingExercise);
  }

  // ============== deleteExercise Tests (5 test cases) ==============

  @Test
  void deleteExercise_WhenInstructorDeletesOwnExercise_ShouldSoftDelete() {
    User instructorUser = getMockUserWithRole(Role.INSTRUCTOR);
    Exercise exercise = getMockExercise();
    exercise.setCreatedBy(instructorUser.getId());

    when(exercisesRepository.findOne(any(Specification.class))).thenReturn(Optional.of(exercise));
    when(exercisesRepository.save(exercise)).thenReturn(exercise);

    exercisesService.deleteExercise("exercise-123", instructorUser);

    verify(exercisesRepository).save(exercise);
  }

  @Test
  void deleteExercise_WhenAdminDeletesExercise_ShouldSoftDelete() {
    User adminUser = getMockUserWithRole(Role.ADMIN);
    Exercise exercise = getMockExercise();

    when(exercisesRepository.findOne(any(Specification.class))).thenReturn(Optional.of(exercise));
    when(exercisesRepository.save(exercise)).thenReturn(exercise);

    exercisesService.deleteExercise("exercise-123", adminUser);

    verify(exercisesRepository).save(exercise);
  }

  @Test
  void deleteExercise_WhenExerciseNotFound_ShouldThrowException() {
    User adminUser = getMockUserWithRole(Role.ADMIN);

    when(exercisesRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

    assertThrows(ExerciseNotFoundException.class, () ->
      exercisesService.deleteExercise("non-existent", adminUser)
    );
    verify(exercisesRepository, never()).save(any(Exercise.class));
  }

  @Test
  void deleteExercise_WhenPublicExercise_ShouldStillSoftDelete() {
    User adminUser = getMockUserWithRole(Role.ADMIN);
    Exercise exercise = getMockExercise();
    exercise.setVisibility(Visibility.PUBLIC);

    when(exercisesRepository.findOne(any(Specification.class))).thenReturn(Optional.of(exercise));
    when(exercisesRepository.save(exercise)).thenReturn(exercise);

    exercisesService.deleteExercise("exercise-123", adminUser);

    verify(exercisesRepository).save(exercise);
  }

  @Test
  void deleteExercise_WhenExerciseHasSubmissions_ShouldStillSoftDelete() {
    User adminUser = getMockUserWithRole(Role.ADMIN);
    Exercise exercise = getMockExercise();
    exercise.setVisibility(Visibility.PUBLIC);
    // Exercise has submissions but should still be deletable (soft delete)

    when(exercisesRepository.findOne(any(Specification.class))).thenReturn(Optional.of(exercise));
    when(exercisesRepository.save(exercise)).thenReturn(exercise);

    exercisesService.deleteExercise("exercise-123", adminUser);

    verify(exercisesRepository).save(exercise);
  }
}
