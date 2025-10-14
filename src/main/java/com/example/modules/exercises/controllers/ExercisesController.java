package com.example.modules.exercises.controllers;

import static com.example.base.utils.AppRoutes.EXERCISES_PREFIX;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.Public;
import com.example.modules.exercises.dtos.ExerciseQueryDTO;
import com.example.modules.exercises.dtos.ExerciseRequestDTO;
import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import com.example.modules.exercises.services.ExercisesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = EXERCISES_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "exercises", description = "Operations related to exercises")
@RequiredArgsConstructor
public class ExercisesController {

  private final ExercisesService exercisesService;

  @Operation(
    summary = "Create new exercise",
    responses = {
      @ApiResponse(responseCode = "201", description = "Exercise created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
    }
  )
  @PostMapping
  @Public
  public ResponseEntity<SuccessResponseDTO<ExerciseResponseDTO>> createExercise(
    @Valid @RequestBody ExerciseRequestDTO request
  ) {
    ExerciseResponseDTO exercise = exercisesService.createExercise(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(
      SuccessResponseDTO.<ExerciseResponseDTO>builder()
        .message("Exercise created successfully")
        .data(exercise)
        .build()
    );
  }

  @Operation(
    summary = "Get exercise by ID",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exercise retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content),
    }
  )
  @GetMapping("/{id}")
  @Public
  public ResponseEntity<SuccessResponseDTO<ExerciseResponseDTO>> getExerciseById(
    @PathVariable String id
  ) {
    ExerciseResponseDTO exercise = exercisesService.getExerciseById(id);
    return ResponseEntity.ok(
      SuccessResponseDTO.<ExerciseResponseDTO>builder()
        .message("Exercise retrieved successfully")
        .data(exercise)
        .build()
    );
  }

  @Operation(
    summary = "Get all exercises with pagination and filters",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully"),
    }
  )
  @GetMapping
  @Public
  public ResponseEntity<PaginatedSuccessResponseDTO<ExerciseResponseDTO>> getExercises(
    @ParameterObject @Valid ExerciseQueryDTO query
  ) {
    PaginatedSuccessResponseDTO<ExerciseResponseDTO> response = exercisesService.getExercises(
      query
    );
    return ResponseEntity.ok(response);
  }

  @Operation(
    summary = "Update exercise",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exercise updated successfully"),
      @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content),
      @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
    }
  )
  @PutMapping("/{id}")
  @Public
  public ResponseEntity<SuccessResponseDTO<ExerciseResponseDTO>> updateExercise(
    @PathVariable String id,
    @Valid @RequestBody ExerciseRequestDTO request
  ) {
    ExerciseResponseDTO exercise = exercisesService.updateExercise(id, request);
    return ResponseEntity.ok(
      SuccessResponseDTO.<ExerciseResponseDTO>builder()
        .message("Exercise updated successfully")
        .data(exercise)
        .build()
    );
  }

  @Operation(
    summary = "Delete exercise",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exercise deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Exercise not found", content = @Content),
    }
  )
  @DeleteMapping("/{id}")
  @Public
  public ResponseEntity<SuccessResponseDTO<Void>> deleteExercise(@PathVariable String id) {
    exercisesService.deleteExercise(id);
    return ResponseEntity.ok(
      SuccessResponseDTO.<Void>builder().message("Exercise deleted successfully").build()
    );
  }
}
