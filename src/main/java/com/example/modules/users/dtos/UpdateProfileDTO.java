package com.example.modules.users.dtos;

import com.example.base.utils.SwaggerExamples;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class UpdateProfileDTO {

  @Schema(
    description = "The roll number of the user",
    example = SwaggerExamples.ROLL_NUMBER,
    implementation = String.class
  )
  @Length(min = 1)
  @Builder.Default
  private JsonNullable<String> rollNumber = JsonNullable.undefined();

  @Schema(
    description = "The first name of the user",
    example = SwaggerExamples.FIRST_NAME,
    implementation = String.class
  )
  @Length(min = 1)
  @Builder.Default
  private JsonNullable<String> firstName = JsonNullable.undefined();

  @Schema(
    description = "The last name of the user",
    example = SwaggerExamples.LAST_NAME,
    implementation = String.class
  )
  @Length(min = 1)
  @Builder.Default
  private JsonNullable<String> lastName = JsonNullable.undefined();

  @Schema(
    description = "The phone number of the user",
    example = SwaggerExamples.PHONE,
    implementation = String.class
  )
  @Length(min = 10, max = 10)
  @Builder.Default
  private JsonNullable<String> phone = JsonNullable.undefined();

  @Schema(description = "The address of the user", implementation = String.class)
  @Length(min = 1)
  @Builder.Default
  private JsonNullable<String> address = JsonNullable.undefined();
}
