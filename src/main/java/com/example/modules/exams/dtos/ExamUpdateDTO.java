package com.example.modules.exams.dtos;

import com.example.base.utils.SwaggerExamples;
import io.jsonwebtoken.lang.Collections;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamUpdateDTO {

  @Schema(implementation = String.class)
  @Builder.Default
  private JsonNullable<String> title = JsonNullable.undefined();

  @Schema(implementation = String.class)
  @Builder.Default
  private JsonNullable<String> description = JsonNullable.undefined();

  @Schema(example = SwaggerExamples.TIMESTAMP)
  @Builder.Default
  private JsonNullable<Instant> startTime = JsonNullable.undefined();

  @Schema(example = SwaggerExamples.TIMESTAMP)
  @Builder.Default
  private JsonNullable<Instant> endTime = JsonNullable.undefined();

  @ArraySchema(schema = @Schema(example = SwaggerExamples.UUID))
  @Builder.Default
  private List<String> groupIds = Collections.emptyList();

  @ArraySchema(schema = @Schema(example = SwaggerExamples.UUID))
  @Builder.Default
  private List<String> exerciseIds = Collections.emptyList();
}
