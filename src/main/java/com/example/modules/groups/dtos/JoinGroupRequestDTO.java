package com.example.modules.groups.dtos;

import com.example.base.utils.SwaggerExamples;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinGroupRequestDTO {

  @Parameter(
    description = "The 8-character join code of the group",
    example = SwaggerExamples.JOIN_CODE
  )
  @NotBlank
  private String code;
}
