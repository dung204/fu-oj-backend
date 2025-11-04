package com.example.modules.exams.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.base.utils.SwaggerExamples;
import com.example.modules.exams.enums.ExamStatus;
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

  @Schema(
    description = "The current status of the exam (e.g., DRAFT, UPCOMING, ONGOING, FINISHED).",
    example = SwaggerExamples.EXAM_STATUS
  )
  private ExamStatus status;

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

  @Schema(
    description = "The unique identifier of the group this exam belongs to.",
    example = SwaggerExamples.UUID
  )
  private String groupId;

  @Schema(
    description = "The name of the group this exam belongs to.",
    example = SwaggerExamples.GROUP_NAME
  )
  private String groupName;

  @Builder.Default
  private List<ExerciseResponseDTO> exercises = List.of();
}
