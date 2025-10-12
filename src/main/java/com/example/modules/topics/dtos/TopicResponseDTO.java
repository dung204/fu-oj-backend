package com.example.modules.topics.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.base.utils.SwaggerExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TopicResponseDTO extends EntityDTO {

  @Schema(description = "The title of the post", example = SwaggerExamples.TOPIC_NAME)
  private String name;

  @Schema(description = "The content of the post", example = SwaggerExamples.DESCRIPTION)
  private String description;
}
