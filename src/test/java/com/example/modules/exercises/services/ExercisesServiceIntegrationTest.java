package com.example.modules.exercises.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.base.BaseServiceIntegrationTest;
import com.example.modules.auth.enums.Role;
import com.example.modules.exercises.dtos.ExerciseQueryDTO;
import com.example.modules.exercises.dtos.ExerciseRequestDTO;
import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.enums.Difficulty;
import com.example.modules.exercises.enums.Visibility;
import com.example.modules.exercises.exceptions.ExerciseNotFoundException;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.test_cases.dtos.TestCaseRequestDTO;
import com.example.modules.topics.entities.Topic;
import com.example.modules.topics.repositories.TopicsRepository;
import com.example.modules.users.entities.User;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

public class ExercisesServiceIntegrationTest extends BaseServiceIntegrationTest {

  @Autowired
  private ExercisesService exercisesService;

  @Autowired
  private ExercisesRepository exercisesRepository;

  @Autowired
  private TopicsRepository topicsRepository;

  private Exercise createTestExercise(User user) {
    Exercise exercise = Exercise.builder()
      .code("TEST001")
      .title("Test Exercise")
      .description("Test Description")
      .difficulty(Difficulty.EASY)
      .visibility(Visibility.PUBLIC)
      .timeLimit(1.0)
      .memory(65536.0)
      .maxSubmissions(10)
      .version(1)
      .createdBy(user.getId())
      .build();

    Exercise saved = exercisesRepository.save(exercise);
    if (saved.getBaseId() == null) {
      saved.setBaseId(saved.getId());
      saved = exercisesRepository.save(saved);
    }
    return saved;
  }

  private Topic createTestTopic() {
    Topic topic = Topic.builder().name("Test Topic").build();
    return topicsRepository.save(topic);
  }

  @Test
  void getExerciseById_ShouldReturnExercise() {
    User user = getUser();
    user.getAccount().setRole(Role.ADMIN);
    Exercise exercise = createTestExercise(user);

    Exercise result = exercisesService.getExerciseById(exercise.getId(), user);

    assertNotNull(result);
    assertEquals(exercise.getId(), result.getId());
    assertEquals("TEST001", result.getCode());
  }

  @Test
  void getExerciseById_WhenNotFound_ShouldThrowException() {
    User user = getUser();
    user.getAccount().setRole(Role.ADMIN);

    assertThrows(ExerciseNotFoundException.class, () ->
      exercisesService.getExerciseById("non-existent-id", user)
    );
  }

  @Test
  void createExercise_ShouldCreateAndReturnExercise() {
    ExerciseRequestDTO request = new ExerciseRequestDTO();
    request.setCode("NEW001");
    request.setTitle("New Exercise");
    request.setDescription("New Description");
    request.setDifficulty("EASY");
    request.setVisibility("PUBLIC");
    request.setTimeLimit(1.0);
    request.setMemory(65536.0);
    request.setMaxSubmissions(10);

    ExerciseResponseDTO result = exercisesService.createExercise(request);

    assertNotNull(result);
    assertEquals("NEW001", result.getCode());
    assertEquals("New Exercise", result.getTitle());
    assertNotNull(result.getId());
  }

  @Test
  void createExercise_WithTopics_ShouldCreateExerciseWithTopics() {
    Topic topic = createTestTopic();

    ExerciseRequestDTO request = new ExerciseRequestDTO();
    request.setCode("NEW002");
    request.setTitle("New Exercise with Topics");
    request.setDescription("New Description");
    request.setDifficulty("EASY");
    request.setVisibility("PUBLIC");
    request.setTopicIds(List.of(topic.getId()));

    ExerciseResponseDTO result = exercisesService.createExercise(request);

    assertNotNull(result);
    assertEquals("NEW002", result.getCode());
    assertNotNull(result.getTopics());
    assertEquals(1, result.getTopics().size());
    assertEquals(topic.getId(), result.getTopics().get(0).getId());
  }

  @Test
  void createExercise_WithInvalidTopicIds_ShouldThrowException() {
    ExerciseRequestDTO request = new ExerciseRequestDTO();
    request.setCode("NEW003");
    request.setTitle("New Exercise");
    request.setDescription("New Description");
    request.setDifficulty("EASY");
    request.setVisibility("PUBLIC");
    request.setTopicIds(List.of("non-existent-topic-id"));

    assertThrows(EntityNotFoundException.class, () -> exercisesService.createExercise(request));
  }

  @Test
  void createExercise_WithTestCases_ShouldCreateExerciseWithTestCases() {
    ExerciseRequestDTO request = new ExerciseRequestDTO();
    request.setCode("NEW004");
    request.setTitle("New Exercise with Test Cases");
    request.setDescription("New Description");
    request.setDifficulty("EASY");
    request.setVisibility("PUBLIC");

    TestCaseRequestDTO testCaseRequest = new TestCaseRequestDTO();
    testCaseRequest.setInput("1 2");
    testCaseRequest.setOutput("3");
    testCaseRequest.setIsPublic(true);
    request.setTestCases(List.of(testCaseRequest));

    ExerciseResponseDTO result = exercisesService.createExercise(request);

    assertNotNull(result);
    assertEquals("NEW004", result.getCode());
    assertNotNull(result.getTestCases());
    assertTrue(result.getTestCases().size() > 0);
  }

  @Test
  void getExercises_ShouldReturnPaginatedExercises() {
    User user = getUser();
    user.getAccount().setRole(Role.ADMIN);
    createTestExercise(user);

    ExerciseQueryDTO query = new ExerciseQueryDTO();
    query.setPage(1);
    query.setPageSize(10);

    Page<ExerciseResponseDTO> result = exercisesService.getExercises(query, user);

    assertNotNull(result);
    assertTrue(result.getTotalElements() > 0);
  }

  @Test
  void updateExercise_WhenDraft_ShouldUpdateInPlace() {
    User user = getUser();
    user.getAccount().setRole(Role.INSTRUCTOR);
    Exercise exercise = createTestExercise(user);
    exercise.setVisibility(Visibility.DRAFT);
    exercise = exercisesRepository.save(exercise);

    ExerciseRequestDTO request = new ExerciseRequestDTO();
    request.setCode(exercise.getCode());
    request.setTitle("Updated Title");
    request.setDescription("Updated Description");
    request.setDifficulty("MEDIUM");
    request.setVisibility("DRAFT");

    ExerciseResponseDTO result = exercisesService.updateExercise(exercise.getId(), request, user);

    assertNotNull(result);
    assertEquals("Updated Title", result.getTitle());
    assertEquals("MEDIUM", result.getDifficulty());
  }

  @Test
  void deleteExercise_ShouldSoftDeleteExercise() {
    User user = getUser();
    user.getAccount().setRole(Role.INSTRUCTOR);
    Exercise exercise = createTestExercise(user);

    exercisesService.deleteExercise(exercise.getId(), user);

    Exercise deleted = exercisesRepository.findById(exercise.getId()).orElse(null);
    assertNotNull(deleted);
    assertNotNull(deleted.getDeletedTimestamp());
  }
}
