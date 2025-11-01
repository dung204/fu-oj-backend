package com.example.modules.exercises.dtos;

import com.example.base.annotations.AllowedStrings;
import com.example.modules.exercises.enums.Difficulty;
import com.example.modules.exercises.enums.Visibility;
import com.example.modules.test_cases.dtos.TestCaseRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

  @AllowedStrings(values = { "PUBLIC", "PRIVATE", "DRAFT" })
  private String visibility = Visibility.DRAFT.getValue();

  @Min(value = 0, message = "Time limit must be at least 0s")
  private Double timeLimit = 0.2;

  @Min(value = 2048, message = "Memory must be at least 2048KB")
  private Double memory = 65536D;

  @AllowedStrings(values = { "EASY", "MEDIUM", "HARD" })
  private String difficulty = Difficulty.EASY.getName();

  @Min(value = 1, message = "Max submissions must be at least 1")
  private Integer maxSubmissions = 10;

  private List<String> topicIds;

  @Valid
  private List<TestCaseRequestDTO> testCases;
}
