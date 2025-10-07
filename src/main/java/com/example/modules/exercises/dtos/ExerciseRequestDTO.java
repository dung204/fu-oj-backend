package com.example.modules.exercises.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseRequestDTO {

  @NotBlank(message = "Code is required")
  private String code;

  @NotBlank(message = "Title is required")
  private String title;

  @NotBlank(message = "Description is required")
  private String description;

  @NotNull(message = "Max submissions is required")
  @Min(value = 1, message = "Max submissions must be at least 1")
  private Integer maxSubmissions;

  private List<String> topicIds;
}
