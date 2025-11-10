package com.example.modules.exams.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.base.utils.SwaggerExamples;
import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponseDTO extends EntityDTO {

  @Schema(description = "The unique identifier of the exam.", example = SwaggerExamples.UUID)
  private String id;

  @Schema(
    description = "A short, unique, human-readable code for the exam.",
    example = SwaggerExamples.EXAM_CODE
  )
  private String code;

  @Schema(description = "The full title of the exam.")
  private String title;

  @Schema(
    description = "A detailed description of the exam, possibly including rules or instructions."
  )
  private String description;

  @Schema(description = "The time limit for completing the exam, in minutes.", example = "90.0")
  private Double timeLimit;

  @Schema(
    description = "The date and time when the exam starts (ISO 8601 format).",
    example = SwaggerExamples.TIMESTAMP
  )
  private String startTime;

  @Schema(
    description = "The date and time when the exam ends (ISO 8601 format).",
    example = SwaggerExamples.TIMESTAMP
  )
  private String endTime;

  @Schema(description = "List of groups this exam belongs to")
  @Builder.Default
  private List<GroupInfo> groups = List.of();

  @Builder.Default
  private List<ExerciseResponseDTO> exercises = List.of();

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class GroupInfo {

    @Schema(description = "Group ID", example = SwaggerExamples.UUID)
    private String id;

    @Schema(description = "Group code", example = "SE1234")
    private String code;

    @Schema(description = "Group name", example = "Software Engineering 2024")
    private String name;

    @Schema(description = "Group description")
    private String description;

    @Schema(description = "Is the group public?")
    private Boolean isPublic;
  }
}
