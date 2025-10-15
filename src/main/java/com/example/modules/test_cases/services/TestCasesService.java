package com.example.modules.test_cases.services;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.utils.ObjectUtils;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.test_cases.dtos.TestCaseQueryDTO;
import com.example.modules.test_cases.dtos.TestCaseRequestDTO;
import com.example.modules.test_cases.dtos.TestCaseResponseDTO;
import com.example.modules.test_cases.entities.TestCase;
import com.example.modules.test_cases.repositories.TestCasesRepository;
import com.example.modules.test_cases.utils.TestCaseMapper;
import com.example.modules.test_cases.utils.TestCasesSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
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
  public TestCaseResponseDTO createTestCase(TestCaseRequestDTO request) {
    // Kiểm tra exercise có tồn tại không
    Exercise exercise = exercisesRepository
      .findById(request.getExerciseId())
      .orElseThrow(() ->
        new EntityNotFoundException("Exercise not found: " + request.getExerciseId())
      );

    TestCase testCase = TestCase.builder()
      .exercise(exercise)
      .input(request.getInput())
      .output(request.getOutput())
      .isPublic(request.getIsPublic())
      .build();

    TestCase savedTestCase = testCasesRepository.save(testCase);
    log.info("Created test case: {}", savedTestCase.getId());

    return testCaseMapper.toTestCaseResponseDTO(savedTestCase);
  }

  /**
   * Lấy test case theo ID
   */
  public TestCaseResponseDTO getTestCaseById(String id) {
    TestCase testCase = testCasesRepository
      .findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Test case not found: " + id));

    return testCaseMapper.toTestCaseResponseDTO(testCase);
  }

  /**
   * Lấy danh sách test cases với pagination và filter
   */
  public PaginatedSuccessResponseDTO<TestCaseResponseDTO> getTestCases(TestCaseQueryDTO query) {
    Specification<TestCase> spec = TestCasesSpecification.buildFilters(
      query.getExerciseId(),
      query.getIsPublic()
    );

    Page<TestCase> testCasePage = testCasesRepository.findAll(spec, query.toPageRequest());
    log.info("Found {} test cases", testCasePage.getTotalElements());

    // Map to DTO
    Page<TestCaseResponseDTO> dtoPage = testCasePage.map(testCaseMapper::toTestCaseResponseDTO);

    return PaginatedSuccessResponseDTO.<TestCaseResponseDTO>builder()
      .message("Test cases retrieved successfully")
      .page(dtoPage)
      .filters(query.getFilters())
      .build();
  }

  /**
   * Cập nhật test case
   */
  @Transactional
  public TestCaseResponseDTO updateTestCase(String id, TestCaseRequestDTO request) {
    TestCase testCase = testCasesRepository
      .findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Test case not found: " + id));

    // Nếu exerciseId thay đổi, kiểm tra exercise mới có tồn tại không
    if (!testCase.getExercise().getId().equals(request.getExerciseId())) {
      Exercise exercise = exercisesRepository
        .findById(request.getExerciseId())
        .orElseThrow(() ->
          new EntityNotFoundException("Exercise not found: " + request.getExerciseId())
        );
      testCase.setExercise(exercise);
    }

    // Update fields
    ObjectUtils.assign(testCase, request);

    TestCase updatedTestCase = testCasesRepository.save(testCase);
    log.info("Updated test case: {}", updatedTestCase.getId());

    return testCaseMapper.toTestCaseResponseDTO(updatedTestCase);
  }

  /**
   * Xóa test case
   */
  @Transactional
  public void deleteTestCase(String id) {
    TestCase testCase = testCasesRepository
      .findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Test case not found: " + id));

    testCasesRepository.delete(testCase);
    log.info("Deleted test case: {}", id);
  }
}
