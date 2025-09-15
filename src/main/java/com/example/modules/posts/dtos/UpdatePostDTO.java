package com.example.modules.posts.dtos;

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
public class UpdatePostDTO {

  @Schema(
    description = "The title of the post",
    example = SwaggerExamples.EMAIL,
    implementation = String.class
  )
  @NotNull
  @Length(max = 255)
  @Builder.Default
  private JsonNullable<String> title = JsonNullable.undefined();

  @Schema(description = "The content of the user", example = SwaggerExamples.EMAIL)
  @NotNull
  @Builder.Default
  private JsonNullable<String> content = JsonNullable.undefined();

  @Schema(description = "Whether the post is public or not", implementation = Boolean.class)
  @Builder.Default
  private JsonNullable<Boolean> isPublic = JsonNullable.undefined();
}
