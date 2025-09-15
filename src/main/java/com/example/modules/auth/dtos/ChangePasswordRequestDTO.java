package com.example.modules.auth.dtos;

import com.example.base.utils.SwaggerExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequestDTO {

  @Schema(
    description = "The old password of the user (not required if the user does not have a password)",
    example = SwaggerExamples.PASSWORD
  )
  private String password;

  @Schema(
    description = "The old password of the user (not required if the user does not have a password)",
    example = SwaggerExamples.PASSWORD
  )
  @NotBlank
  @Size(min = 6)
  private String newPassword;
}
