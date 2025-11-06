package com.example.modules.exercises.controllers;

import static com.example.base.utils.AppRoutes.EXERCISES_PREFIX;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.enums.Role;
import com.example.modules.comments.dtos.CommentByExerciseQueryDTO;
import com.example.modules.comments.dtos.CommentCreateDTO;
import com.example.modules.comments.dtos.CommentResponseDTO;
import com.example.modules.comments.services.CommentsService;
import com.example.modules.exercises.dtos.ExerciseQueryDTO;
import com.example.modules.exercises.dtos.ExerciseRequestDTO;
import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.services.ExercisesService;
import com.example.modules.exercises.utils.ExerciseMapper;
import com.example.modules.test_cases.dtos.TestCaseQueryDTO;
import com.example.modules.test_cases.dtos.TestCaseRequestDTO;
import com.example.modules.test_cases.dtos.TestCaseResponseDTO;
import com.example.modules.test_cases.services.TestCasesService;
import com.example.modules.users.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = EXERCISES_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "exercises", description = "Operations related to exercises")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExercisesController {

  ExercisesService exercisesService;
  ExerciseMapper exerciseMapper;
  TestCasesService testCasesService;
  CommentsService commentsService;

  @Operation(
    summary = "Get all exercises with pagination and filters",
    description = "Retrieves a paginated list of the latest version of exercises based on the user's role:\n\n" +
      "*   **ADMIN**: Can view all exercises, including soft-deleted ones.\n" +
      "*   **INSTRUCTOR**: Can view non-deleted public exercises and non-deleted exercises they have created.\n" +
      "*   **STUDENT**: Can only view non-deleted exercises that are part of a group they are a member of.\n\n" +
      "For `STUDENT` roles, the details of private test cases will be hidden.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully"),
    }
  )
  @GetMapping
  public PaginatedSuccessResponseDTO<ExerciseResponseDTO> getExercises(
    @ParameterObject @Valid ExerciseQueryDTO query,
    @CurrentUser User currentUser
  ) {
    return PaginatedSuccessResponseDTO.<ExerciseResponseDTO>builder()
      .message("Exercises retrieved successfully")
      .page(exercisesService.getExercises(query, currentUser))
      .filters(query.getFilters())
      .build();
  }

  @Operation(
    summary = "Get all exercises with pagination and filters (for testing purposes, to be removed in the future)",
    description = "Retrieves a paginated list of the latest version of exercises.\n" +
      "This endpoint does not contain access logic, and should be used for testing purposes.\n" +
      "For `STUDENT` roles, the details of private test cases will be hidden.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully"),
    }
  )
  @GetMapping("/temp")
  public PaginatedSuccessResponseDTO<ExerciseResponseDTO> temp_getExercises(
    @ParameterObject @Valid ExerciseQueryDTO query,
    @CurrentUser User currentUser
  ) {
    return PaginatedSuccessResponseDTO.<ExerciseResponseDTO>builder()
      .message("Exercises retrieved successfully")
      .page(exercisesService.temp_getExercises(query, currentUser))
      .filters(query.getFilters())
      .build();
  }

  @Operation(
    summary = "Get exercise by ID",
    description = "Retrieves an exercise by its ID. Access is determined by the user's role:\n\n" +
      "*   **ADMIN**: Can access any exercise, including soft-deleted ones.\n" +
      "*   **INSTRUCTOR**: Can access non-deleted public exercises or non-deleted exercises they created.\n" +
      "*   **STUDENT**: Can access non-deleted public exercises or non-deleted exercises belonging to groups they are a member of.\n\n" +
      "For `STUDENT` roles, the details of private test cases will be hidden (`input`, `output`, `note` will become `null`).\n\n" +
      "A `404 Not Found` error is returned if the exercise does not exist or if the user is not authorized to access it.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exercise retrieved successfully"),
      @ApiResponse(
        responseCode = "404",
        description = "Exercise not found or user not authorized to access the exercise",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{exerciseId}")
  public SuccessResponseDTO<ExerciseResponseDTO> getExerciseById(
    @PathVariable String exerciseId,
    @CurrentUser User currentUser
  ) {
    Exercise exercise = exercisesService.getExerciseById(exerciseId, currentUser);
    ExerciseResponseDTO exerciseResponseDTO = currentUser.getAccount().getRole() == Role.STUDENT
      ? exerciseMapper.toExerciseResponseDTOWithPrivateTestCasesHidden(exercise)
      : exerciseMapper.toExerciseResponseDTOWithAllTestCases(exercise);

    return SuccessResponseDTO.<ExerciseResponseDTO>builder()
      .message("Exercise retrieved successfully")
      .data(exerciseResponseDTO)
      .build();
  }

  @Operation(
    summary = "Get exercise by ID (for testing purposes, to be removed in the future)",
    description = "Retrieves an exercise by its ID.\n\n" +
      "This endpoint does not contain access logic, and should be used for testing purposes.\n",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exercise retrieved successfully"),
      @ApiResponse(
        responseCode = "404",
        description = "Exercise not found or user not authorized to access the exercise",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{exerciseId}/temp")
  public SuccessResponseDTO<ExerciseResponseDTO> temp_getExerciseById(
    @PathVariable String exerciseId,
    @CurrentUser User currentUser
  ) {
    Exercise exercise = exercisesService.temp_getExerciseById(exerciseId);
    ExerciseResponseDTO exerciseResponseDTO = currentUser.getAccount().getRole() == Role.STUDENT
      ? exerciseMapper.toExerciseResponseDTOWithPrivateTestCasesHidden(exercise)
      : exerciseMapper.toExerciseResponseDTOWithAllTestCases(exercise);

    return SuccessResponseDTO.<ExerciseResponseDTO>builder()
      .message("Exercise retrieved successfully")
      .data(exerciseResponseDTO)
      .build();
  }

  @AllowRoles({ Role.INSTRUCTOR, Role.ADMIN })
  @Operation(
    summary = "Create new exercise (for INSTRUCTOR and ADMIN)",
    description = "Creates a new exercise. This endpoint is available to users with the `INSTRUCTOR` or `ADMIN` role.",
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
    description = "Updates an exercise by creating a new version. The user must be an `INSTRUCTOR` and have access to the original exercise.\n\n" +
      "**Access Logic:**\n" +
      "*   An `INSTRUCTOR` can access non-deleted public exercises or non-deleted exercises they created.\n\n" +
      "A `404 Not Found` error is returned if the exercise does not exist or if the user is not authorized to access it.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exercise updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = "User's role is not `INSTRUCTOR`",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Exercise not found or user not authorized to access the exercise",
        content = @Content
      ),
    }
  )
  @PutMapping("/{exerciseId}")
  public SuccessResponseDTO<ExerciseResponseDTO> updateExercise(
    @PathVariable String exerciseId,
    @Valid @RequestBody ExerciseRequestDTO request,
    @CurrentUser User currentUser
  ) {
    ExerciseResponseDTO exercise = exercisesService.updateExercise(
      exerciseId,
      request,
      currentUser
    );
    return SuccessResponseDTO.<ExerciseResponseDTO>builder()
      .message("Exercise updated successfully")
      .data(exercise)
      .build();
  }

  @AllowRoles(Role.INSTRUCTOR)
  @Operation(
    summary = "Delete exercise (for INSTRUCTOR only)",
    description = "Performs a soft delete on an exercise. The user must be an `INSTRUCTOR` and have access to the exercise.\n\n" +
      "**Access Logic:**\n" +
      "*   An `INSTRUCTOR` can access non-deleted public exercises or non-deleted exercises they created.\n\n" +
      "A `404 Not Found` error is returned if the exercise does not exist or if the user is not authorized to access it.",
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
      @ApiResponse(
        responseCode = "404",
        description = "Exercise not found or user not authorized to access the exercise",
        content = @Content
      ),
    }
  )
  @DeleteMapping("/{exerciseId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteExercise(@PathVariable String exerciseId, @CurrentUser User currentUser) {
    exercisesService.deleteExercise(exerciseId, currentUser);
  }

  @AllowRoles({ Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Get all test cases of an exercise (for ADMIN and INSTRUCTOR only)",
    description = "Retrieves all test cases for a specific exercise. Access to the exercise is first verified based on the user's role:\n\n" +
      "*   **ADMIN**: Can access any exercise, including soft-deleted ones.\n" +
      "*   **INSTRUCTOR**: Can access non-deleted public exercises or non-deleted exercises they created.\n\n" +
      "A `404 Not Found` error is returned if the exercise does not exist or if the user is not authorized to access it.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Test cases retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = "User's role is not `ADMIN` or `INSTRUCTOR`",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Exercise not found or user not authorized",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{exerciseId}/test-cases")
  public PaginatedSuccessResponseDTO<TestCaseResponseDTO> getAllTestCasesOfAnExercise(
    @PathVariable String exerciseId,
    @ParameterObject @Valid TestCaseQueryDTO query,
    @CurrentUser User currentUser
  ) {
    return PaginatedSuccessResponseDTO.<TestCaseResponseDTO>builder()
      .message("Test cases retrieved successfully")
      .page(testCasesService.getTestCasesOfExercise(exerciseId, query, currentUser))
      .filters(query.getFilters())
      .build();
  }

  @AllowRoles({ Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Get a test case by ID (for ADMIN and INSTRUCTOR only)",
    description = "Retrieves a single test case by its ID, scoped to a specific exercise. Access to the exercise is first verified based on the user's role:\n\n" +
      "*   **ADMIN**: Can access any exercise, including soft-deleted ones.\n" +
      "*   **INSTRUCTOR**: Can access non-deleted public exercises or non-deleted exercises they created.\n\n" +
      "A `404 Not Found` error is returned if the exercise or test case does not exist, or if the user is not authorized to access the exercise.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Test case retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = "User's role is not `ADMIN` or `INSTRUCTOR`",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Exercise or test case not found, or user not authorized",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{exerciseId}/test-cases/{testCaseId}")
  public SuccessResponseDTO<TestCaseResponseDTO> getTestCaseOfAnExercise(
    @PathVariable String exerciseId,
    @PathVariable String testCaseId,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<TestCaseResponseDTO>builder()
      .message("Test case retrieved successfully")
      .data(testCasesService.getTestCaseByIdAndExerciseId(testCaseId, exerciseId, currentUser))
      .build();
  }

  @AllowRoles({ Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Create a test case for an exercise (for ADMIN and INSTRUCTOR only)",
    description = "Creates a new test case for a specific exercise. Access to the exercise is first verified based on the user's role:\n\n" +
      "*   **ADMIN**: Can access any exercise, including soft-deleted ones.\n" +
      "*   **INSTRUCTOR**: Can access non-deleted public exercises or non-deleted exercises they created.\n\n" +
      "A `404 Not Found` error is returned if the exercise does not exist or if the user is not authorized to access it.",
    responses = {
      @ApiResponse(responseCode = "201", description = "Test case created successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = "User's role is not `ADMIN` or `INSTRUCTOR`",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Exercise not found or user not authorized",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping("/{exerciseId}/test-cases")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<TestCaseResponseDTO> createTestCaseOfAnExercise(
    @PathVariable String exerciseId,
    @RequestBody @Valid TestCaseRequestDTO request,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<TestCaseResponseDTO>builder()
      .message("Test case created successfully")
      .data(testCasesService.createTestCase(exerciseId, request, currentUser))
      .build();
  }

  @AllowRoles({ Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Update a test case of an exercise (for ADMIN and INSTRUCTOR only)",
    description = "Updates an existing test case for a specific exercise. Access to the exercise is first verified based on the user's role:\n\n" +
      "*   **ADMIN**: Can access any exercise, including soft-deleted ones.\n" +
      "*   **INSTRUCTOR**: Can access non-deleted public exercises or non-deleted exercises they created.\n\n" +
      "A `404 Not Found` error is returned if the exercise or test case does not exist, or if the user is not authorized to access the exercise.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Test case updated successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = "User's role is not `ADMIN` or `INSTRUCTOR`",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Exercise or test case not found, or user not authorized",
        content = @Content
      ),
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
    description = "Deletes a test case from a specific exercise. Access to the exercise is first verified based on the user's role:\n\n" +
      "*   **ADMIN**: Can access any exercise, including soft-deleted ones.\n" +
      "*   **INSTRUCTOR**: Can access non-deleted public exercises or non-deleted exercises they created.\n\n" +
      "A `404 Not Found` error is returned if the exercise or test case does not exist, or if the user is not authorized to access the exercise.",
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
      @ApiResponse(
        responseCode = "404",
        description = "Exercise or test case not found, or user not authorized",
        content = @Content
      ),
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

  @Operation(
    summary = "Retrieve a list of comments for an exercise",
    description = "Fetches a paginated list of comments for a specific exercise. Access to the exercise is determined by the user's role:\n\n" +
      "*   **ADMIN**: Can access any exercise, including soft-deleted ones.\n" +
      "*   **INSTRUCTOR**: Can access non-deleted public exercises or non-deleted exercises they created.\n" +
      "*   **STUDENT**: Can access non-deleted public exercises or non-deleted exercises belonging to groups they are a member of.\n\n" +
      "A `404 Not Found` error is returned if the exercise does not exist or if the user is not authorized to access it.\n\n" +
      "The returned comments depend on the provided optional parent comment ID:\n" +
      "  * If `parentId = 'null'` (string with content of 'null'), returns all top-level comments of the exercise.\n" +
      "  * If `parentId` are provided, returns all replies to the specified parent comment.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
      @ApiResponse(
        responseCode = "404",
        description = "Exercise or comment not found",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{exerciseId}/comments")
  public PaginatedSuccessResponseDTO<CommentResponseDTO> getComments(
    @PathVariable String exerciseId,
    @ParameterObject @Valid CommentByExerciseQueryDTO commentByExerciseQueryDTO,
    @CurrentUser User currentUser
  ) {
    return PaginatedSuccessResponseDTO.<CommentResponseDTO>builder()
      .status(200)
      .message("Retrieved comments successfully")
      .page(
        commentsService.getCommentsByParentIdAndExerciseId(
          exerciseId,
          commentByExerciseQueryDTO,
          currentUser
        )
      )
      .filters(commentByExerciseQueryDTO.getFilters())
      .build();
  }

  @Operation(
    summary = "Create a new comment on an exercise",
    description = "Creates a new comment on a specific exercise. Access to the exercise is determined by the user's role:\n\n" +
      "*   **ADMIN**: Can access any exercise, including soft-deleted ones.\n" +
      "*   **INSTRUCTOR**: Can access non-deleted public exercises or non-deleted exercises they created.\n" +
      "*   **STUDENT**: Can access non-deleted public exercises or non-deleted exercises belonging to groups they are a member of.\n\n" +
      "A `404 Not Found` error is returned if the exercise does not exist or if the user is not authorized to access it.",
    responses = {
      @ApiResponse(responseCode = "201", description = "Comment created successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Some fields in request body is invalid",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "401",
        description = "Create new comment is fail",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Exercise not found or user not authorized",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping("/{exerciseId}/comments")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<CommentResponseDTO> createComment(
    @PathVariable String exerciseId,
    @RequestBody @Valid CommentCreateDTO commentRequestDTO,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<CommentResponseDTO>builder()
      .status(201)
      .message("Comment created successfully")
      .data(commentsService.createComment(exerciseId, commentRequestDTO, currentUser))
      .build();
  }
}
