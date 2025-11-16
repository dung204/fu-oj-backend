package com.example.modules.ai.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExerciseGenerationRequest {

  @Min(value = 1, message = "Number of exercises must be at least 1")
  Integer numberOfExercise;

  @NotEmpty(message = "At least one difficulty level is required")
  List<String> level; // ["EASY", "MEDIUM", "HARD"]

  @NotBlank(message = "Topic is required")
  String topic; // topic ID

  @NotBlank(message = "Prompt is required")
  String prompt;
}
