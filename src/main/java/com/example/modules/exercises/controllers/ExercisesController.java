package com.example.modules.exercises.controllers;

import static com.example.base.utils.AppRoutes.EXERCISES_PREFIX;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.enums.Role;
import com.example.modules.exercises.dtos.ExerciseQueryDTO;
import com.example.modules.exercises.dtos.ExerciseRequestDTO;
import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import com.example.modules.exercises.services.ExercisesService;
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
@RequestMapping(path = EXERCISES_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "exercises", description = "Operations related to exercises")
@RequiredArgsConstructor
public class ExercisesController {

  private final ExercisesService exercisesService;
  private final TestCasesService testCasesService;

  @Operation(
    summary = "Get all exercises with pagination and filters",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully"),
    }
  )
  @GetMapping
  public PaginatedSuccessResponseDTO<ExerciseResponseDTO> getExercises(
    @ParameterObject @Valid ExerciseQueryDTO query
  ) {
    return PaginatedSuccessResponseDTO.<ExerciseResponseDTO>builder()
      .message("Exercises retrieved successfully")
      .page(exercisesService.getExercises(query))
      .filters(query.getFilters())
      .build();
  }

  @Operation(
    summary = "Get exercise by ID (for students - only public test cases)",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exercise retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{exerciseId}")
  public SuccessResponseDTO<ExerciseResponseDTO> getExerciseById(@PathVariable String exerciseId) {
    ExerciseResponseDTO exercise = exercisesService.getExerciseById(exerciseId);
    return SuccessResponseDTO.<ExerciseResponseDTO>builder()
      .message("Exercise retrieved successfully")
      .data(exercise)
      .build();
  }

  @AllowRoles({ Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Get exercise by ID with all test cases (for ADMIN and INSTRUCTOR only)",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exercise retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = "User's role is not `ADMIN` or `INSTRUCTOR`",
        content = @Content
      ),
      @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{exerciseId}/full")
  public SuccessResponseDTO<ExerciseResponseDTO> getExerciseByIdFull(
    @PathVariable String exerciseId
  ) {
    ExerciseResponseDTO exercise = exercisesService.getExerciseByIdWithAllTestCases(exerciseId);
    return SuccessResponseDTO.<ExerciseResponseDTO>builder()
      .message("Exercise retrieved successfully with all test cases")
      .data(exercise)
      .build();
  }

  @AllowRoles(Role.INSTRUCTOR)
  @Operation(
    summary = "Create new exercise (for INSTRUCTOR only)",
    responses = {
      @ApiResponse(responseCode = "201", description = "Exercise created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = "User's role is not `INSTRUCTOR`",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "409",
        description = "Exercise code already exists",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<ExerciseResponseDTO> createExercise(
    @Valid @RequestBody ExerciseRequestDTO request
  ) {
    ExerciseResponseDTO exercise = exercisesService.createExercise(request);
    return SuccessResponseDTO.<ExerciseResponseDTO>builder()
      .message("Exercise created successfully")
      .data(exercise)
      .build();
  }

  @AllowRoles(Role.INSTRUCTOR)
  @Operation(
    summary = "Update exercise (for INSTRUCTOR only)",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exercise updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = "User's role is not `INSTRUCTOR`",
        content = @Content
      ),
      @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content),
    }
  )
  @PutMapping("/{exerciseId}")
  public SuccessResponseDTO<ExerciseResponseDTO> updateExercise(
    @PathVariable String exerciseId,
    @Valid @RequestBody ExerciseRequestDTO request
  ) {
    ExerciseResponseDTO exercise = exercisesService.updateExercise(exerciseId, request);
    return SuccessResponseDTO.<ExerciseResponseDTO>builder()
      .message("Exercise updated successfully")
      .data(exercise)
      .build();
  }

  @AllowRoles(Role.INSTRUCTOR)
  @Operation(
    summary = "Delete exercise (for INSTRUCTOR only)",
    responses = {
      @ApiResponse(
        responseCode = "204",
        description = "Exercise deleted successfully",
        content = @Content
      ),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = "User's role is not `INSTRUCTOR`",
        content = @Content
      ),
      @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content),
    }
  )
  @DeleteMapping("/{exerciseId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteExercise(@PathVariable String exerciseId) {
    exercisesService.deleteExercise(exerciseId);
  }

  @AllowRoles({ Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Get all test cases of an exercise with pagination and filters (for ADMIN and INSTRUCTOR only)",
    responses = {
      @ApiResponse(responseCode = "200", description = "Test cases retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = "User's role is not `ADMIN` or `INSTRUCTOR`",
        content = @Content
      ),
      @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{exerciseId}/test-cases")
  public PaginatedSuccessResponseDTO<TestCaseResponseDTO> getAllTestCasesOfAnExercise(
    @PathVariable String exerciseId,
    @ParameterObject @Valid TestCaseQueryDTO query
  ) {
    return PaginatedSuccessResponseDTO.<TestCaseResponseDTO>builder()
      .message("Test cases retrieved successfully")
      .page(testCasesService.getTestCasesOfExercise(exerciseId, query))
      .filters(query.getFilters())
      .build();
  }

  @AllowRoles({ Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Get a test case by test case ID and exercise ID (for ADMIN and INSTRUCTOR only)",
    responses = {
      @ApiResponse(responseCode = "200", description = "Test case retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = "User's role is not `ADMIN` or `INSTRUCTOR`",
        content = @Content
      ),
      @ApiResponse(responseCode = "404", description = "Test case not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{exerciseId}/test-cases/{testCaseId}")
  public SuccessResponseDTO<TestCaseResponseDTO> getTestCaseOfAnExercise(
    @PathVariable String exerciseId,
    @PathVariable String testCaseId
  ) {
    return SuccessResponseDTO.<TestCaseResponseDTO>builder()
      .message("Test case retrieved successfully")
      .data(testCasesService.getTestCaseByIdAndExerciseId(testCaseId, exerciseId))
      .build();
  }

  @AllowRoles({ Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Create a test case of an exercise (for ADMIN and INSTRUCTOR only)",
    responses = {
      @ApiResponse(responseCode = "201", description = "Test case created successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = "User's role is not `ADMIN` or `INSTRUCTOR`",
        content = @Content
      ),
      @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping("/{exerciseId}/test-cases")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<TestCaseResponseDTO> createTestCaseOfAnExercise(
    @PathVariable String exerciseId,
    @RequestBody @Valid TestCaseRequestDTO request
  ) {
    return SuccessResponseDTO.<TestCaseResponseDTO>builder()
      .message("Test case created successfully")
      .data(testCasesService.createTestCase(exerciseId, request))
      .build();
  }

  @AllowRoles({ Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Update a test case of an exercise (for ADMIN and INSTRUCTOR only)",
    responses = {
      @ApiResponse(responseCode = "200", description = "Test case updated successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = "User's role is not `ADMIN` or `INSTRUCTOR`",
        content = @Content
      ),
      @ApiResponse(responseCode = "404", description = "Test case not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PutMapping("/{exerciseId}/test-cases/{testCaseId}")
  public SuccessResponseDTO<TestCaseResponseDTO> updateTestCaseOfAnExercise(
    @PathVariable String exerciseId,
    @PathVariable String testCaseId,
    @RequestBody @Valid TestCaseRequestDTO request
  ) {
    return SuccessResponseDTO.<TestCaseResponseDTO>builder()
      .message("Test case updated successfully")
      .data(testCasesService.updateTestCase(exerciseId, testCaseId, request))
      .build();
  }

  @AllowRoles({ Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Delete a test case of an exercise (for ADMIN and INSTRUCTOR only)",
    responses = {
      @ApiResponse(
        responseCode = "204",
        description = "Test case deleted successfully",
        content = @Content
      ),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = "User's role is not `ADMIN` or `INSTRUCTOR`",
        content = @Content
      ),
      @ApiResponse(responseCode = "404", description = "Test case not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @DeleteMapping("/{exerciseId}/test-cases/{testCaseId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTestCaseOfAnExercise(
    @PathVariable String exerciseId,
    @PathVariable String testCaseId
  ) {
    testCasesService.deleteTestCase(exerciseId, testCaseId);
  }
}
