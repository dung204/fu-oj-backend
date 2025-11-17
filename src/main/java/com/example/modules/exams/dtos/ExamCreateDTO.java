package com.example.modules.exams.dtos;

import com.example.base.annotations.AllowedStrings;
import com.example.base.utils.SwaggerExamples;
import com.example.modules.exams.enums.ExamStatus;
import io.jsonwebtoken.lang.Collections;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamCreateDTO {

  @NotBlank
  private String title;

  @NotBlank
  private String description;

  @NotNull
  private Instant startTime;

  @NotNull
  private Instant endTime;

  private Double timeLimit;

  @Schema(example = SwaggerExamples.EXAM_STATUS)
  @AllowedStrings(values = { "DRAFT", "UPCOMING" })
  private String status = ExamStatus.DRAFT.getValue();

  @ArraySchema(schema = @Schema(example = SwaggerExamples.UUID))
  private List<String> groupIds = Collections.emptyList();

  @ArraySchema(schema = @Schema(example = SwaggerExamples.UUID))
  private List<String> exerciseIds = Collections.emptyList();
}
