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
import com.example.modules.topics.entities.Topic;
import com.example.modules.topics.repositories.TopicsRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
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
  private final ExerciseMapper exerciseMapper;

  /**
   * Tạo mới exercise
   */
  @Transactional
  public ExerciseResponseDTO createExercise(ExerciseRequestDTO request) {
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
    if (request.getTopicIds() != null && !request.getTopicIds().isEmpty()) {
      // filter duplicate IDS
      List<String> uniqueTopicIds = request.getTopicIds().stream().distinct().toList();
      // get topics from DB
      List<Topic> topics = topicsRepository.findAllById(uniqueTopicIds);
      if (topics.size() != request.getTopicIds().size()) {
        throw new EntityNotFoundException("Some topic IDs not found");
      }
      exercise.setTopics(topics);
    }

    Exercise savedExercise = exercisesRepository.save(exercise);
    log.info("Created exercise: {}", savedExercise.getId());

    return exerciseMapper.toExerciseResponseDTO(savedExercise);
  }

  /**
   * Lấy exercise theo ID
   */
  public ExerciseResponseDTO getExerciseById(String id) {
    Exercise exercise = exercisesRepository
      .findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Exercise not found: " + id));

    return exerciseMapper.toExerciseResponseDTO(exercise);
  }

  /**
   * Lấy danh sách exercises với pagination và filter
   */
  public PaginatedSuccessResponseDTO<ExerciseResponseDTO> getExercises(ExerciseQueryDTO query) {
    Specification<Exercise> spec = ExercisesSpecification.buildOrFilters(
      query.getCode(),
      query.getTitle(),
      query.getTopicIds(),
      query.getGroupId()
    );

    Page<Exercise> exercisePage = exercisesRepository.findAll(spec, query.toPageRequest());
    log.info("Found {} exercises", exercisePage.getTotalElements());

    // Map to DTO
    Page<ExerciseResponseDTO> dtoPage = exercisePage.map(exercise -> {
      ExerciseResponseDTO dto = exerciseMapper.toExerciseResponseDTO(exercise);

      if (dto.getTestCases() != null) {
        dto.setTestCases(
          dto.getTestCases().stream().filter(TestCaseResponseDTO::getIsPublic).toList()
        );
      } else {
        dto.setTestCases(new ArrayList<>());
      }

      return dto;
    });

    return PaginatedSuccessResponseDTO.<ExerciseResponseDTO>builder()
      .message("Exercises retrieved successfully")
      .page(dtoPage)
      .filters(query.getFilters())
      .build();
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

    return exerciseMapper.toExerciseResponseDTO(updatedExercise);
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
