package com.example.modules.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.base.BaseServiceTest;
import com.example.modules.ai.dtos.request.ExerciseGenerationRequest;
import com.example.modules.ai.dtos.response.ExerciseGenerationResponse;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.topics.entities.Topic;
import com.example.modules.topics.repositories.TopicsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

class ExerciseGenerationServiceTest extends BaseServiceTest {

  @Mock
  private ChatClient.Builder chatClientBuilder;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ChatClient chatClient;

  @Mock
  private TopicsRepository topicsRepository;

  @Mock
  private ExercisesRepository exercisesRepository;

  private ExerciseGenerationService exerciseGenerationService;

  @BeforeEach
  void setUp() {
    when(chatClientBuilder.build()).thenReturn(chatClient);
    exerciseGenerationService = new ExerciseGenerationService(
      chatClientBuilder,
      new ObjectMapper(),
      topicsRepository,
      exercisesRepository
    );
  }

  @Test
  void generateExercises_WhenTestCaseCountsInvalid_ShouldThrowException() {
    ExerciseGenerationRequest request = buildRequest();
    request.setNumberOfPublicTestCases(3);
    request.setNumberOfPrivateTestCases(3);
    request.setTotalTestCasesPerExercise(5);

    RuntimeException exception = assertThrows(RuntimeException.class, () ->
      exerciseGenerationService.generateExercises(request)
    );

    assertTrue(exception.getMessage().contains("Tổng số test case public"));
  }

  @Test
  void generateExercises_WhenTopicNotFound_ShouldThrowException() {
    ExerciseGenerationRequest request = buildRequest();
    when(topicsRepository.findById("topic-1")).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class, () ->
      exerciseGenerationService.generateExercises(request)
    );

    assertTrue(exception.getMessage().contains("Không tìm thấy topic"));
  }

  @Test
  void generateExercises_WhenAiReturnsValidResponse_ShouldParseExercises() {
    ExerciseGenerationRequest request = buildRequest();
    Topic topic = Topic.builder().id("topic-1").name("Array").build();

    when(topicsRepository.findById("topic-1")).thenReturn(Optional.of(topic));
    when(
      exercisesRepository.findAll(
        ArgumentMatchers.<Specification<Exercise>>any(),
        any(Pageable.class)
      )
    ).thenReturn(new PageImpl<>(List.of(Exercise.builder().id("ex-1").code("ARR-1").build())));
    when(chatClient.prompt(any(Prompt.class)).call().content()).thenReturn(
      """
      [
        {
          "code": "ARR-123",
          "title": "Two Sum",
          "description": "Find pair",
          "solution": "public class Main {}",
          "difficulty": "EASY",
          "timeLimit": 0.2,
          "memory": 65000,
          "maxSubmissions": 0,
          "testCases": [
            { "input": "1 2", "output": "3", "isPublic": true },
            { "input": "2 3", "output": "5", "isPublic": false }
          ]
        }
      ]
      """
    );

    ExerciseGenerationResponse response = exerciseGenerationService.generateExercises(request);

    assertEquals(1, response.getExercises().size());
    assertEquals("ARR-123", response.getExercises().get(0).getCode());
    assertEquals("topic-1", response.getExercises().get(0).getTopicIds().get(0));
  }

  private ExerciseGenerationRequest buildRequest() {
    ExerciseGenerationRequest request = new ExerciseGenerationRequest();
    request.setNumberOfExercise(1);
    request.setLevel(List.of("EASY"));
    request.setTopic("topic-1");
    request.setNumberOfPublicTestCases(1);
    request.setNumberOfPrivateTestCases(1);
    request.setTotalTestCasesPerExercise(2);
    request.setSolutionLanguage("java");
    request.setVisibility("DRAFT");
    return request;
  }
}
