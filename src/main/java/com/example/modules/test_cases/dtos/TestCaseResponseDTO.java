package com.example.modules.test_cases.dtos;

import com.example.base.dtos.EntityDTO;
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
public class TestCaseResponseDTO extends EntityDTO {

  @Schema(description = "The input data for the test case.")
  private String input;

  @Schema(description = "The expected output for the given input.")
  private String output;

  @Schema(description = "An optional note or explanation for the test case.")
  private String note;

  @Schema(
    description = "Indicates if the test case is public (visible to students as a sample) or hidden (used for judging)."
  )
  private Boolean isPublic;
}
