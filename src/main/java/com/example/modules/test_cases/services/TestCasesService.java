package com.example.modules.test_cases.services;

import com.example.base.utils.ObjectUtils;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.exceptions.ExerciseNotFoundException;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.test_cases.dtos.TestCaseQueryDTO;
import com.example.modules.test_cases.dtos.TestCaseRequestDTO;
import com.example.modules.test_cases.dtos.TestCaseResponseDTO;
import com.example.modules.test_cases.entities.TestCase;
import com.example.modules.test_cases.exceptions.TestCaseNotFoundException;
import com.example.modules.test_cases.repositories.TestCasesRepository;
import com.example.modules.test_cases.utils.TestCaseMapper;
import com.example.modules.test_cases.utils.TestCasesSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestCasesService {

  private final TestCasesRepository testCasesRepository;
  private final ExercisesRepository exercisesRepository;
  private final TestCaseMapper testCaseMapper;

  /**
   * Tạo mới test case
   */
  @Transactional
  public TestCaseResponseDTO createTestCase(String exerciseId, TestCaseRequestDTO request) {
    // Kiểm tra exercise có tồn tại không
    Exercise exercise = exercisesRepository
      .findById(exerciseId)
      .orElseThrow(ExerciseNotFoundException::new);

    TestCase testCase = TestCase.builder()
      .exercise(exercise)
      .input(request.getInput())
      .output(request.getOutput())
      .isPublic(request.getIsPublic())
      .build();

    return testCaseMapper.toTestCaseResponseDTO(testCasesRepository.save(testCase));
  }

  public TestCaseResponseDTO getTestCaseByIdAndExerciseId(String testCaseId, String exerciseId) {
    return testCaseMapper.toTestCaseResponseDTO(
      testCasesRepository
        .findOne(
          TestCasesSpecification.builder().withExerciseId(exerciseId).withId(testCaseId).build()
        )
        .orElseThrow(TestCaseNotFoundException::new)
    );
  }

  public Page<TestCaseResponseDTO> getTestCasesOfExercise(
    String exerciseId,
    TestCaseQueryDTO query
  ) {
    exercisesRepository
      .findById(exerciseId)
      .orElseThrow(() -> new EntityNotFoundException("Exercise not found: " + exerciseId));

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
