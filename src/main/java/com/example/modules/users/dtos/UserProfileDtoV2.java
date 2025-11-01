package com.example.modules.users.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.base.utils.SwaggerExamples;
import com.example.modules.minio.dtos.MinioFileResponse;
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
public class UserProfileDtoV2 extends EntityDTO {

  @Schema(
    description = "The roll number of the student, `null` for INSTRUCTOR & ADMIN",
    example = SwaggerExamples.EMAIL
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
}
