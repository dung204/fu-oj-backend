package com.example.modules.courses.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseUpdateDTO {

  @Schema(implementation = String.class)
  @Builder.Default
  private JsonNullable<String> title = JsonNullable.undefined();

  @Schema(implementation = String.class)
  @Builder.Default
  private JsonNullable<String> description = JsonNullable.undefined();

  @Schema(implementation = String.class)
  @Builder.Default
  private JsonNullable<String> certificateTemplateName = JsonNullable.undefined();
}
