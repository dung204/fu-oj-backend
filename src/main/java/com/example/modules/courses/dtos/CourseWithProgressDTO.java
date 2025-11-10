package com.example.modules.courses.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CourseWithProgressDTO extends CourseResponseDTO {

  private Progress progress;

  @Data
  @Builder
  public static class Progress {

    @Schema(
      description = "The number of exercises the user has successfully solved in this course."
    )
    private Long solvedCount;

    @Schema(description = "The total number of exercises in this course.")
    private Long totalCount;

    @Schema(
      description = "The user's completion percentage for the course, calculated as (solvedCount / totalCount)."
    )
    private Double percentage;

    @Schema(
      description = "A boolean flag indicating whether the user has completed all exercises in the course."
    )
    private Boolean isCompleted;
  }
}
