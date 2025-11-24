package com.example.modules.exercises.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExercisesVisibilityRequestDTO {

  @NotNull(message = "Exercise IDs cannot be null")
  @NotEmpty(message = "Exercise IDs cannot be empty")
  private List<String> exerciseIds;

  @NotNull(message = "Visibility cannot be null")
  @Pattern(
    regexp = "PUBLIC|PRIVATE|DRAFT",
    message = "Visibility must be PUBLIC, PRIVATE, or DRAFT"
  )
  private String visibility;
}
