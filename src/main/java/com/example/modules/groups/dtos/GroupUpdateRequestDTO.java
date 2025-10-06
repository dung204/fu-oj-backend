package com.example.modules.groups.dtos;

import com.example.base.utils.SwaggerExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupUpdateRequestDTO {

  @Schema(description = "The Id of group", example = SwaggerExamples.UUID)
  private String id;

  @Schema(description = "The name of group", example = SwaggerExamples.GROUP_NAME)
  @NotBlank
  private String name;

  @Schema(description = "The description of group", example = SwaggerExamples.DESCRIPTION)
  private String description;
}
