package com.example.modules.certificates.dtos;

import com.example.base.utils.SwaggerExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateIssueDTO {

  @Schema(
    description = "The unique identifier of the student to whom the certificate will be issued.",
    example = SwaggerExamples.UUID
  )
  @NotBlank
  private String studentId;

  @Schema(
    description = "The unique identifier of the course for which the certificate is being issued.",
    example = SwaggerExamples.UUID
  )
  @NotBlank
  private String courseId;

  @Schema(
    description = "An optional reason or note for issuing the certificate, useful for manual issuance or special cases."
  )
  private String reason;
}
