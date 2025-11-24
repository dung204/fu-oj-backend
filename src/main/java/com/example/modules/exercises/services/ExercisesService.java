package com.example.modules.exercises.services;

import com.example.base.utils.ObjectUtils;
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
import com.example.modules.exercises.utils.ExercisesSpecification;
import com.example.modules.groups.entities.Group;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.test_cases.entities.TestCase;
import com.example.modules.test_cases.repositories.TestCasesRepository;
import com.example.modules.topics.entities.Topic;
import com.example.modules.topics.repositories.TopicsRepository;
import com.example.modules.users.entities.User;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExercisesService {

  ExercisesRepository exercisesRepository;
  TopicsRepository topicsRepository;
  TestCasesRepository testCasesRepository;
  ExerciseMapper exerciseMapper;
  SubmissionRepository submissionRepository;

  /**
   * Lấy exercise theo ID (chỉ lấy public test cases - dành cho student)
   */
  public Exercise getExerciseById(String id, User currentUser) {
    Optional<Exercise> exercise = Optional.ofNullable(null);

    switch (currentUser.getAccount().getRole()) {
      case Role.ADMIN:
        exercise = exercisesRepository.findOne(ExercisesSpecification.builder().withId(id).build());
        break;
      case Role.INSTRUCTOR:
        exercise = exercisesRepository.findOne(
          ExercisesSpecification.builder()
            .or(ExercisesSpecification::publicOnly, spec -> spec.createdBy(currentUser.getId()))
            .withId(id)
            .notDeleted()
            .build()
        );
        break;
      case Role.STUDENT:
        exercise = exercisesRepository.findOne(
          ExercisesSpecification.builder()
            .or(ExercisesSpecification::publicOnly, spec ->
              spec.inOneOfGroups(currentUser.getJoinedGroups().stream().map(Group::getId).toList())
            )
            .withId(id)
            .notDeleted()
            .build()
        );
        break;
    }

    return exercise.orElseThrow(ExerciseNotFoundException::new);
  }

  /**
   * Tạo mới exercise
   */
  @Transactional
  public ExerciseResponseDTO createExercise(ExerciseRequestDTO request) {
    // Kiểm tra code đã tồn tại chưa
    Specification<Exercise> codeSpec = ExercisesSpecification.builder()
      .withCode(request.getCode())
      .build();

    if (exercisesRepository.exists(codeSpec)) {
      throw new IllegalArgumentException("Exercise code already exists: " + request.getCode());
    }

    Exercise exercise = Exercise.builder()
      .code(request.getCode())
      .title(request.getTitle())
      .description(request.getDescription())
      .maxSubmissions(request.getMaxSubmissions())
      .difficulty(Difficulty.valueOf(request.getDifficulty()))
      .visibility(Visibility.valueOf(request.getVisibility()))
      .timeLimit(request.getTimeLimit())
      .memory(request.getMemory())
      .solution(request.getSolution())
      .version(1)
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

    Exercise savedExercise = exercisesRepository.save(exercise);
    // Set baseId sau khi save để có id
    if (savedExercise.getBaseId() == null) {
      savedExercise.setBaseId(savedExercise.getId());
      savedExercise = exercisesRepository.save(savedExercise);
    }
    final Exercise finalSavedExercise = savedExercise;
    log.info("Created exercise: {}", finalSavedExercise.getId());

    // Tạo test cases nếu có trong request
    if (request.getTestCases() != null) {
      List<TestCase> testCases = request
        .getTestCases()
        .stream()
        .map(testCaseRequest ->
          TestCase.builder()
            .exercise(finalSavedExercise)
            .input(testCaseRequest.getInput())
            .output(testCaseRequest.getOutput())
            .isPublic(testCaseRequest.getIsPublic())
            .build()
        )
        .collect(Collectors.toList());

      finalSavedExercise.setTestCases(testCasesRepository.saveAll(testCases));
      log.info(
        "Created {} test cases for exercise: {}",
        testCases.size(),
        finalSavedExercise.getId()
      );
    }

    return exerciseMapper.toExerciseResponseDTOWithAllTestCases(finalSavedExercise);
  }

  /**
   * Lấy danh sách exercises với pagination và filter
   */
  public Page<ExerciseResponseDTO> getExercises(ExerciseQueryDTO dto, User currentUser) {
    Page<Exercise> exercisesPage = null;

    switch (currentUser.getAccount().getRole()) {
      case Role.ADMIN:
        exercisesPage = exercisesRepository.findAll(
          ExercisesSpecification.builder()
            .<ExercisesSpecification>or(
              spec -> spec.containsCode(dto.getQuery()),
              spec -> spec.containsTitle(dto.getQuery())
            )
            .hasOneOfTopics(dto.getTopic())
            .onlyLatestVersion()
            .build(),
          dto.toPageRequest()
        );
        break;
      case Role.INSTRUCTOR:
        exercisesPage = exercisesRepository.findAll(
          ExercisesSpecification.builder()
            .or(ExercisesSpecification::publicOnly, spec -> spec.createdBy(currentUser.getId()))
            .<ExercisesSpecification>or(
              spec -> spec.containsCode(dto.getQuery()),
              spec -> spec.containsTitle(dto.getQuery())
            )
            .hasOneOfTopics(dto.getTopic())
            .onlyLatestVersion()
            .notDeleted()
            .build(),
          dto.toPageRequest()
        );
        break;
      case Role.STUDENT:
        exercisesPage = exercisesRepository.findAll(
          ExercisesSpecification.builder()
            .or(ExercisesSpecification::publicOnly, spec ->
              spec.inOneOfGroups(currentUser.getJoinedGroups().stream().map(Group::getId).toList())
            )
            .<ExercisesSpecification>or(
              spec -> spec.containsCode(dto.getQuery()),
              spec -> spec.containsTitle(dto.getQuery())
            )
            .hasOneOfTopics(dto.getTopic())
            .onlyLatestVersion()
            .notDeleted()
            .build(),
          dto.toPageRequest()
        );
        break;
    }

    log.info("Found {} exercises", exercisesPage.getTotalElements());

    // Map to DTO
    return exercisesPage.map(
      currentUser.getAccount().getRole() == Role.STUDENT
        ? exerciseMapper::toExerciseResponseDTOWithPrivateTestCasesHidden
        : exerciseMapper::toExerciseResponseDTOWithAllTestCases
    );
  }

  public Page<ExerciseResponseDTO> getExercisesByCourseId(
    String courseId,
    ExerciseQueryDTO dto,
    User currentUser
  ) {
    Page<Exercise> exercisesPage = exercisesRepository.findAll(
      ExercisesSpecification.builder()
        .withCourseId(courseId)
        .<ExercisesSpecification>or(
          spec -> spec.containsCode(dto.getQuery()),
          spec -> spec.containsTitle(dto.getQuery())
        )
        .hasOneOfTopics(dto.getTopic())
        .onlyLatestVersion()
        .build(),
      dto.toPageRequest()
    );

    return exercisesPage.map(
      currentUser.getAccount().getRole() == Role.STUDENT
        ? exerciseMapper::toExerciseResponseDTOWithPrivateTestCasesHidden
        : exerciseMapper::toExerciseResponseDTOWithAllTestCases
    );
  }

  /**
   * Cập nhật exercise
   */
  @Transactional
  public ExerciseResponseDTO updateExercise(
    String id,
    ExerciseRequestDTO request,
    User currentUser
  ) {
    Exercise oldExercise = getExerciseById(id, currentUser);

    // if the exercise is draft or no submissions, allow in-place update
    Submission submission = submissionRepository.getSubmissionByExercise(oldExercise);

    if (oldExercise.getVisibility().equals(Visibility.DRAFT) || submission == null) {
      // Kiểm tra trùng code nếu code thay đổi
      if (!oldExercise.getCode().equals(request.getCode())) {
        boolean exists = exercisesRepository.existsByCode((request.getCode()));
        if (exists) {
          throw new IllegalArgumentException("Exercise code already exists: " + request.getCode());
        }
      }

      // Gán các thay đổi mới từ request
      ObjectUtils.assign(oldExercise, request);

      // Update topics
      if (request.getTopicIds() != null) {
        if (request.getTopicIds().isEmpty()) {
          oldExercise.setTopics(new ArrayList<>());
        } else {
          List<Topic> topics = topicsRepository.findAllById(request.getTopicIds());
          if (topics.size() != request.getTopicIds().size()) {
            throw new EntityNotFoundException("Some topic IDs not found");
          }
          oldExercise.setTopics(topics);
        }
      }

      Exercise savedExercise = exercisesRepository.save(oldExercise);
      log.info("Updated exercise in-place: {}", savedExercise.getId());

      return exerciseMapper.toExerciseResponseDTOWithAllTestCases(savedExercise);
    }

    // Kiểm tra trùng code nếu code thay đổi
    if (!oldExercise.getCode().equals(request.getCode())) {
      boolean exists = exercisesRepository.existsByCode((request.getCode()));
      if (exists) {
        throw new IllegalArgumentException("Exercise code already exists: " + request.getCode());
      }
    }

    // Tạo bản version mới (clone)
    Exercise newVersion = new Exercise();
    BeanUtils.copyProperties(
      oldExercise,
      newVersion,
      "id",
      "createdAt",
      "updatedAt",
      "testCases",
      "topics",
      "groups",
      "examExercises"
    );

    newVersion.setId(null);
    newVersion.setVersion(oldExercise.getVersion() + 1);
    newVersion.setBaseId(
      oldExercise.getBaseId() != null ? oldExercise.getBaseId() : oldExercise.getId()
    );

    // Gán các thay đổi mới từ request
    ObjectUtils.assign(newVersion, request);

    // Update topics (clone lại mối quan hệ)
    if (request.getTopicIds() != null) {
      if (request.getTopicIds().isEmpty()) {
        newVersion.setTopics(new ArrayList<>());
      } else {
        List<Topic> topics = topicsRepository.findAllById(request.getTopicIds());
        if (topics.size() != request.getTopicIds().size()) {
          throw new EntityNotFoundException("Some topic IDs not found");
        }
        newVersion.setTopics(topics);
      }
    } else {
      // Tạo ArrayList mới để tránh "shared reference" collection error
      newVersion.setTopics(
        oldExercise.getTopics() != null
          ? new ArrayList<>(oldExercise.getTopics())
          : new ArrayList<>()
      );
    }

    Exercise savedExercise = exercisesRepository.save(newVersion);
    log.info(
      "Created new version (v{}) for exercise {} → {}",
      newVersion.getVersion(),
      oldExercise.getId(),
      savedExercise.getId()
    );

    // Clone test cases (thay vì update)
    List<TestCase> oldTestCases = testCasesRepository.findAllByExerciseId(oldExercise.getId());
    for (TestCase oldCase : oldTestCases) {
      TestCase clone = new TestCase();
      BeanUtils.copyProperties(oldCase, clone, "id", "createdAt", "updatedAt", "exercise");
      clone.setExercise(savedExercise);
      testCasesRepository.save(clone);
    }

    // Nếu request có testCases (thêm mới)
    if (request.getTestCases() != null) {
      for (var testCaseRequest : request.getTestCases()) {
        if (testCaseRequest.getId() == null || testCaseRequest.getId().isEmpty()) {
          TestCase newTestCase = TestCase.builder()
            .exercise(savedExercise)
            .input(testCaseRequest.getInput())
            .output(testCaseRequest.getOutput())
            .isPublic(testCaseRequest.getIsPublic())
            .build();
          testCasesRepository.save(newTestCase);
          log.info("Added new test case for exercise version {}", savedExercise.getVersion());
        }
      }
    }

    return exerciseMapper.toExerciseResponseDTOWithAllTestCases(savedExercise);
  }

  /**
   * Xóa exercise
   */
  @Transactional
  public void deleteExercise(String id, User currentUser) {
    Exercise exercise = getExerciseById(id, currentUser);
    exercise.softDelete();

    exercisesRepository.save(exercise);
    log.info("Deleted exercise: {}", id);
  }

  /**
   * Cập nhật visibility cho nhiều exercises
   */
  @Transactional
  public void updateExercisesVisibility(List<String> exerciseIds, String visibilityValue) {
    Visibility visibility = Visibility.getValue(visibilityValue);
    int updatedCount = 0;

    for (String exerciseId : exerciseIds) {
      try {
        Optional<Exercise> exerciseOpt = exercisesRepository.findById(exerciseId);

        if (exerciseOpt.isEmpty()) {
          log.warn("Exercise with ID {} not found, skipping", exerciseId);
          continue;
        }

        Exercise exercise = exerciseOpt.get();
        exercise.setVisibility(visibility);
        exercisesRepository.save(exercise);
        updatedCount++;

        log.info("Updated visibility for exercise {} to {}", exerciseId, visibilityValue);
      } catch (Exception e) {
        log.error("Error updating exercise {}: {}", exerciseId, e.getMessage());
      }
    }

    log.info("Updated visibility for {} out of {} exercises", updatedCount, exerciseIds.size());
  }
}
