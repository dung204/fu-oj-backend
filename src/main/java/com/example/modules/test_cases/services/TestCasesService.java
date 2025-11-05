package com.example.modules.test_cases.services;

import com.example.base.utils.ObjectUtils;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.services.ExercisesService;
import com.example.modules.test_cases.dtos.TestCaseQueryDTO;
import com.example.modules.test_cases.dtos.TestCaseRequestDTO;
import com.example.modules.test_cases.dtos.TestCaseResponseDTO;
import com.example.modules.test_cases.entities.TestCase;
import com.example.modules.test_cases.exceptions.TestCaseNotFoundException;
import com.example.modules.test_cases.repositories.TestCasesRepository;
import com.example.modules.test_cases.utils.TestCaseMapper;
import com.example.modules.test_cases.utils.TestCasesSpecification;
import com.example.modules.users.entities.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestCasesService {

  TestCasesRepository testCasesRepository;
  ExercisesService exercisesService;
  TestCaseMapper testCaseMapper;

  /**
   * Tạo mới test case
   */
  @Transactional
  public TestCaseResponseDTO createTestCase(
    String exerciseId,
    TestCaseRequestDTO request,
    User currentUser
  ) {
    // Kiểm tra exercise có tồn tại không
    Exercise exercise = exercisesService.getExerciseById(exerciseId, currentUser);

    TestCase testCase = TestCase.builder()
      .exercise(exercise)
      .input(request.getInput())
      .output(request.getOutput())
      .isPublic(request.getIsPublic())
      .build();

    return testCaseMapper.toTestCaseResponseDTO(testCasesRepository.save(testCase));
  }

  public TestCaseResponseDTO getTestCaseByIdAndExerciseId(
    String testCaseId,
    String exerciseId,
    User currentUser
  ) {
    Exercise exercise = exercisesService.getExerciseById(exerciseId, currentUser);

    return testCaseMapper.toTestCaseResponseDTO(
      testCasesRepository
        .findOne(
          TestCasesSpecification.builder()
            .withExerciseId(exercise.getId())
            .withId(testCaseId)
            .build()
        )
        .orElseThrow(TestCaseNotFoundException::new)
    );
  }

  public Page<TestCaseResponseDTO> getTestCasesOfExercise(
    String exerciseId,
    TestCaseQueryDTO query,
    User currentUser
  ) {
    exercisesService.getExerciseById(exerciseId, currentUser);

    return testCasesRepository
      .findAll(
        TestCasesSpecification.buildFilters(exerciseId, query.getIsPublic()),
        query.toPageRequest()
      )
      .map(testCaseMapper::toTestCaseResponseDTO);
  }

  @Transactional
  public TestCaseResponseDTO updateTestCase(
    String exerciseId,
    String testCaseId,
    TestCaseRequestDTO request
  ) {
    TestCase testCase = testCasesRepository
      .findOne(
        TestCasesSpecification.builder().withExerciseId(exerciseId).withId(testCaseId).build()
      )
      .orElseThrow(TestCaseNotFoundException::new);

    ObjectUtils.assign(testCase, request);

    return testCaseMapper.toTestCaseResponseDTO(testCasesRepository.save(testCase));
  }

  /**
   * Xóa test case
   */
  @Transactional
  public void deleteTestCase(String exerciseId, String testCaseId) {
    TestCase testCase = testCasesRepository
      .findById(testCaseId)
      .orElseThrow(TestCaseNotFoundException::new);

    testCasesRepository.delete(testCase);
  }
}
