package com.example.modules.exercises.dtos;

import com.example.base.dtos.PaginatedQueryDTO;
import com.example.modules.test_cases.dtos.TestCaseRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExerciseRequestDTO extends PaginatedQueryDTO {

  @NotBlank(message = "Code is required")
  private String code;

  @NotBlank(message = "Title is required")
  private String title;

  @NotBlank(message = "Description is required")
  private String description;

  private String visibility;

  private Double timeLimit;

  private Double memory;

  private String difficulty;

  @NotNull(message = "Max submissions is required")
  @Min(value = 0, message = "Max submissions must be at least 0")
  private Integer maxSubmissions;

  private List<String> topicIds;

  @Valid
  private List<TestCaseRequestDTO> testCases;
}
