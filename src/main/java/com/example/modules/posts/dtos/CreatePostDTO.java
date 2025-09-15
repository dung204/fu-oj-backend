package com.example.modules.posts.dtos;

import com.example.base.utils.SwaggerExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
public class CreatePostDTO {

  @Schema(description = "The title of the post", example = SwaggerExamples.EMAIL)
  @NotBlank
  @Length(max = 255)
  private String title;

  @Schema(description = "The content of the user", example = SwaggerExamples.EMAIL)
  @NotBlank
  private String content;

  @Schema(description = "Whether the post is public or not", implementation = Boolean.class)
  @Builder.Default
  private JsonNullable<Boolean> isPublic = JsonNullable.undefined();
}
