package com.example.modules.exercises.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.base.utils.SwaggerExamples;
import com.example.modules.test_cases.dtos.TestCaseResponseDTO;
import com.example.modules.topics.dtos.TopicResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseResponseDTO extends EntityDTO {

  @Schema(
    description = "A short, unique, human-readable code for the exercise.",
    example = SwaggerExamples.EXERCISE_CODE
  )
  private String code;

  @Schema(description = "The full title of the exercise.")
  private String title;

  @Schema(
    description = "A detailed description of the exercise, including the problem statement, input/output format, and constraints. Supports Markdown."
  )
  private String description;

  @Schema(
    description = "The maximum execution time allowed for a submission, in seconds.",
    example = SwaggerExamples.TIME_LIMIT
  )
  private Double timeLimit;

  @Schema(
    description = "The maximum memory allowed for a submission, in kilobytes (KB).",
    example = SwaggerExamples.MEMORY
  )
  private Double memory;

  @Schema(
    description = "The visibility status of the exercise (e.g., DRAFT, PUBLIC, PRIVATE).",
    example = SwaggerExamples.VISIBILITY
  )
  private String visibility;

  @Schema(
    description = "The difficulty level of the exercise (e.g., EASY, MEDIUM, HARD).",
    example = SwaggerExamples.DIFFICULTY
  )
  private String difficulty;

  @Schema(
    description = "The maximum number of submissions allowed for this exercise.",
    example = "1000000"
  )
  private Integer maxSubmissions;

  @Schema(
    description = "The version number of the exercise. Increments when the exercise is updated.",
    example = "1"
  )
  private Integer version;

  @Schema(
    description = "The official solution code for the exercise. Typically only visible to instructors or admins."
  )
  private String solution;

  @Schema(
    description = "The unique identifier of the base exercise this version was created from. All versions of an exercise share the same baseId.",
    example = SwaggerExamples.UUID
  )
  private String baseId;

  // default empty list to avoid null pointer exception
  @Builder.Default
  private List<TopicResponseDTO> topics = List.of();

  @Builder.Default
  private List<TestCaseResponseDTO> testCases = List.of();

  private Integer testCasesCount;

  @Schema(
    description = "Indicates whether the current user has completed this exercise based on their best score and the exercise difficulty.",
    example = "false"
  )
  @Builder.Default
  private Boolean solved = Boolean.FALSE;
}
