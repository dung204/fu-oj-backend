package com.example.modules.users.dtos;

import com.example.base.utils.SwaggerExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openapitools.jackson.nullable.JsonNullable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileDTO {

  @Schema(
    description = "The first name of the user",
    example = SwaggerExamples.FIRST_NAME,
    implementation = String.class
  )
  @Size(min = 2)
  @Builder.Default
  private JsonNullable<String> firstName = JsonNullable.undefined();

  @Schema(
    description = "The last name of the user",
    example = SwaggerExamples.LAST_NAME,
    implementation = String.class
  )
  @Size(min = 2)
  @Builder.Default
  private JsonNullable<String> lastName = JsonNullable.undefined();
}
