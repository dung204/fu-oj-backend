package com.example.modules.users.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.base.utils.SwaggerExamples;
import com.example.modules.minio.dtos.MinioFileResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.openapitools.jackson.nullable.JsonNullable;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO extends EntityDTO {

  @Schema(
    description = "The roll number of the student, `null` for INSTRUCTOR & ADMIN",
    example = SwaggerExamples.ROLL_NUMBER
  )
  private String rollNumber;

  @Schema(description = "The email of the user", example = SwaggerExamples.EMAIL)
  private String email;

  @Schema(description = "The role of the user", example = SwaggerExamples.ROLE)
  private String role;

  @Schema(
    description = "The first name of the user",
    example = SwaggerExamples.FIRST_NAME,
    nullable = true
  )
  private String firstName;

  @Schema(
    description = "The last name of the user",
    example = SwaggerExamples.LAST_NAME,
    nullable = true
  )
  private String lastName;

  @Schema(
    description = "The phone number of the user",
    example = SwaggerExamples.PHONE,
    implementation = String.class
  )
  @Size(min = 10, max = 10)
  private String phone;

  @Schema(description = "The address of the user", implementation = String.class)
  private String address;

  @Schema(description = "The avatar of the user", implementation = MinioFileResponse.class)
  private MinioFileResponse avatar;
}
