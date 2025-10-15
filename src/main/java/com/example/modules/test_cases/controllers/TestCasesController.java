package com.example.modules.test_cases.controllers;

import static com.example.base.utils.AppRoutes.TEST_CASES_PREFIX;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.Public;
import com.example.modules.test_cases.dtos.TestCaseQueryDTO;
import com.example.modules.test_cases.dtos.TestCaseRequestDTO;
import com.example.modules.test_cases.dtos.TestCaseResponseDTO;
import com.example.modules.test_cases.services.TestCasesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = TEST_CASES_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "test_cases", description = "Operations related to test cases")
@RequiredArgsConstructor
public class TestCasesController {

  private final TestCasesService testCasesService;

  @Operation(
    summary = "Create new test case",
    responses = {
      @ApiResponse(responseCode = "201", description = "Test case created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
      @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content),
    }
  )
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Public
  public SuccessResponseDTO<TestCaseResponseDTO> createTestCase(
    @Valid @RequestBody TestCaseRequestDTO request
  ) {
    TestCaseResponseDTO testCase = testCasesService.createTestCase(request);
    return SuccessResponseDTO.<TestCaseResponseDTO>builder()
      .message("Test case created successfully")
      .data(testCase)
      .build();
  }

  @Operation(
    summary = "Get test case by ID",
    responses = {
      @ApiResponse(responseCode = "200", description = "Test case retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found", content = @Content),
    }
  )
  @GetMapping("/{id}")
  @Public
  public SuccessResponseDTO<TestCaseResponseDTO> getTestCaseById(@PathVariable String id) {
    TestCaseResponseDTO testCase = testCasesService.getTestCaseById(id);
    return SuccessResponseDTO.<TestCaseResponseDTO>builder()
      .message("Test case retrieved successfully")
      .data(testCase)
      .build();
  }

  @Operation(
    summary = "Get all test cases with pagination and filters",
    responses = {
      @ApiResponse(responseCode = "200", description = "Test cases retrieved successfully"),
    }
  )
  @GetMapping
  @Public
  public PaginatedSuccessResponseDTO<TestCaseResponseDTO> getTestCases(
    @ParameterObject @Valid TestCaseQueryDTO query
  ) {
    return testCasesService.getTestCases(query);
  }

  @Operation(
    summary = "Update test case",
    responses = {
      @ApiResponse(responseCode = "200", description = "Test case updated successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found", content = @Content),
      @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
    }
  )
  @PutMapping("/{id}")
  @Public
  public SuccessResponseDTO<TestCaseResponseDTO> updateTestCase(
    @PathVariable String id,
    @Valid @RequestBody TestCaseRequestDTO request
  ) {
    TestCaseResponseDTO testCase = testCasesService.updateTestCase(id, request);
    return SuccessResponseDTO.<TestCaseResponseDTO>builder()
      .message("Test case updated successfully")
      .data(testCase)
      .build();
  }

  @Operation(
    summary = "Delete test case",
    responses = {
      @ApiResponse(responseCode = "200", description = "Test case deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Test case not found", content = @Content),
    }
  )
  @DeleteMapping("/{id}")
  @Public
  public SuccessResponseDTO<Void> deleteTestCase(@PathVariable String id) {
    testCasesService.deleteTestCase(id);
    return SuccessResponseDTO.<Void>builder().message("Test case deleted successfully").build();
  }
}
