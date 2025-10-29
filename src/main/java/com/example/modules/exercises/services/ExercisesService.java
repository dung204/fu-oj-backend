package com.example.modules.exercises.services;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.utils.ObjectUtils;
import com.example.modules.exercises.dtos.ExerciseQueryDTO;
import com.example.modules.exercises.dtos.ExerciseRequestDTO;
import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.exercises.utils.ExerciseMapper;
import com.example.modules.exercises.utils.ExercisesSpecification;
import com.example.modules.test_cases.dtos.TestCaseResponseDTO;
import com.example.modules.test_cases.entities.TestCase;
import com.example.modules.test_cases.repositories.TestCasesRepository;
import com.example.modules.topics.entities.Topic;
import com.example.modules.topics.repositories.TopicsRepository;
import com.example.modules.users.entities.User;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExercisesService {

  private final ExercisesRepository exercisesRepository;
  private final TopicsRepository topicsRepository;
  private final TestCasesRepository testCasesRepository;
  private final ExerciseMapper exerciseMapper;

  /**
   * Tạo mới exercise
   */
  @Transactional
  public ExerciseResponseDTO createExercise(ExerciseRequestDTO request, User currentUser) {
    // Kiểm tra code đã tồn tại chưa
    Specification<Exercise> codeSpec = ExercisesSpecification.builder()
      .withCode(request.getCode())
      .build();

    if (exercisesRepository.findAll(codeSpec).stream().findFirst().isPresent()) {
      throw new IllegalArgumentException("Exercise code already exists: " + request.getCode());
    }

    Exercise exercise = Exercise.builder()
      .code(request.getCode())
      .title(request.getTitle())
      .description(request.getDescription())
      .maxSubmissions(request.getMaxSubmissions())
      .build();

    // Gắn topics nếu có
    if (request.getTopicIds() != null) {
      // filter duplicate IDS
      List<String> uniqueTopicIds = request.getTopicIds().stream().distinct().toList();
      // get topics from DB
      List<Topic> topics = topicsRepository.findAllById(uniqueTopicIds);
      if (topics.size() != request.getTopicIds().size()) {
        throw new EntityNotFoundException("Some topic IDs not found");
      }
      exercise.setTopics(topics);
    }

    // assign id createdBy
    exercise.setCreatedBy(currentUser.getId());

    Exercise savedExercise = exercisesRepository.save(exercise);
    log.info("Created exercise: {}", savedExercise.getId());

    // Tạo test cases nếu có trong request
    if (request.getTestCases() != null) {
      List<TestCase> testCases = request
        .getTestCases()
        .stream()
        .map(testCaseRequest ->
          TestCase.builder()
            .exercise(savedExercise)
            .input(testCaseRequest.getInput())
            .output(testCaseRequest.getOutput())
            .isPublic(testCaseRequest.getIsPublic())
            .build()
        )
        .collect(Collectors.toList());

      savedExercise.setTestCases(testCasesRepository.saveAll(testCases));
      log.info("Created {} test cases for exercise: {}", testCases.size(), savedExercise.getId());
    }

    return exerciseMapper.toExerciseResponseDTOWithAllTestCases(savedExercise);
  }

  /**
   * Lấy exercise theo ID (chỉ lấy public test cases - dành cho student)
   */
  public ExerciseResponseDTO getExerciseById(String id) {
    Exercise exercise = exercisesRepository
      .findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Exercise not found: " + id));

    return exerciseMapper.toExerciseResponseDTOWithPrivateTestCasesHidden(exercise);
  }

  /**
   * Lấy exercise theo ID với tất cả test cases (dành cho instructor/admin)
   */
  public ExerciseResponseDTO getExerciseByIdWithAllTestCases(String id) {
    Exercise exercise = exercisesRepository
      .findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Exercise not found: " + id));

    return exerciseMapper.toExerciseResponseDTOWithAllTestCases(exercise);
  }

  /**
   * Lấy danh sách exercises với pagination và filter
   */
  public Page<ExerciseResponseDTO> getExercises(ExerciseQueryDTO dto) {
    Page<Exercise> exercisePage = exercisesRepository.findAll(
      ExercisesSpecification.builder()
        .containsCodeOrContainsTitle(dto.getQuery())
        .hasOneOfTopics(dto.getTopic())
        .build(),
      dto.toPageRequest()
    );
    log.info("Found {} exercises", exercisePage.getTotalElements());

    // Map to DTO
    return exercisePage.map(exerciseMapper::toExerciseResponseDTOWithPrivateTestCasesHidden);
  }

  /**
   * Cập nhật exercise
   */
  @Transactional
  public ExerciseResponseDTO updateExercise(String id, ExerciseRequestDTO request) {
    Exercise exercise = exercisesRepository
      .findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Exercise not found: " + id));

    // Kiểm tra code duplicate (nếu thay đổi)
    if (!exercise.getCode().equals(request.getCode())) {
      Specification<Exercise> codeSpec = ExercisesSpecification.builder()
        .withCode(request.getCode())
        .build();

      if (exercisesRepository.findAll(codeSpec).stream().findFirst().isPresent()) {
        throw new IllegalArgumentException("Exercise code already exists: " + request.getCode());
      }
    }

    // Update fields
    ObjectUtils.assign(exercise, request);

    // Update topics
    if (request.getTopicIds() != null) {
      if (request.getTopicIds().isEmpty()) {
        exercise.setTopics(new ArrayList<>());
      } else {
        List<Topic> topics = topicsRepository.findAllById(request.getTopicIds());
        if (topics.size() != request.getTopicIds().size()) {
          throw new EntityNotFoundException("Some topic IDs not found");
        }
        exercise.setTopics(topics);
      }
    }

    Exercise updatedExercise = exercisesRepository.save(exercise);
    log.info("Updated exercise: {}", updatedExercise.getId());

    // Xử lý test cases nếu có trong request
    if (request.getTestCases() != null) {
      for (var testCaseRequest : request.getTestCases()) {
        if (testCaseRequest.getId() != null && !testCaseRequest.getId().isEmpty()) {
          // Update test case hiện có
          TestCase existingTestCase = testCasesRepository
            .findById(testCaseRequest.getId())
            .orElseThrow(() ->
              new EntityNotFoundException("Test case not found: " + testCaseRequest.getId())
            );

          // Kiểm tra test case có thuộc exercise này không
          if (!existingTestCase.getExercise().getId().equals(updatedExercise.getId())) {
            throw new IllegalArgumentException("Test case does not belong to this exercise");
          }

          existingTestCase.setInput(testCaseRequest.getInput());
          existingTestCase.setOutput(testCaseRequest.getOutput());
          existingTestCase.setIsPublic(testCaseRequest.getIsPublic());
          testCasesRepository.save(existingTestCase);
          log.info("Updated test case: {}", existingTestCase.getId());
        } else {
          // Tạo mới test case
          TestCase newTestCase = TestCase.builder()
            .exercise(updatedExercise)
            .input(testCaseRequest.getInput())
            .output(testCaseRequest.getOutput())
            .isPublic(testCaseRequest.getIsPublic())
            .build();
          testCasesRepository.save(newTestCase);
          log.info("Created new test case for exercise: {}", updatedExercise.getId());
        }
      }
    }

    return exerciseMapper.toExerciseResponseDTOWithAllTestCases(updatedExercise);
  }

  /**
   * Xóa exercise
   */
  @Transactional
  public void deleteExercise(String id) {
    Exercise exercise = exercisesRepository
      .findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Exercise not found: " + id));

    exercisesRepository.delete(exercise);
    log.info("Deleted exercise: {}", id);
  }
}
