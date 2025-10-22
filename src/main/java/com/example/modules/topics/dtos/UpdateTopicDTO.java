package com.example.modules.topics.dtos;

import com.example.base.utils.SwaggerExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.openapitools.jackson.nullable.JsonNullable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTopicDTO {

  @Schema(
    description = "The title of the topic",
    example = SwaggerExamples.TITLE,
    implementation = String.class
  )
  @NotNull
  @Length(max = 255)
  @Builder.Default
  private JsonNullable<String> name = JsonNullable.undefined();

  @Schema(description = "The description of the topic", example = SwaggerExamples.DESCRIPTION)
  @NotNull
  @Builder.Default
  private JsonNullable<String> description = JsonNullable.undefined();
}
