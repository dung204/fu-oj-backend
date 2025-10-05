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
public class GroupRequestDTO {

  @Schema(description = "The name of group", example = SwaggerExamples.GROUP_NAME)
  @NotBlank
  private String name;

  @Schema(description = "The description of group", example = SwaggerExamples.DESCRIPTION)
  private String description;

  @Schema(description = "The id of instructor created group", example = SwaggerExamples.OWNER_ID)
  @NotBlank
  private String ownerId;
}
