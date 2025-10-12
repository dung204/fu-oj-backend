package com.example.modules.topics.dtos;

import com.example.base.utils.SwaggerExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTopicDTO {

  @Schema(description = "The name of the topic", example = SwaggerExamples.TITLE)
  @NotBlank
  @Length(max = 255)
  private String name;

  @Schema(description = "The description of the topic", example = SwaggerExamples.DESCRIPTION)
  @NotBlank
  private String description;
}
