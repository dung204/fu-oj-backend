package com.example.modules.certificates.controllers;

import static com.example.base.utils.AppRoutes.CERTIFICATES_PREFIX;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.enums.Role;
import com.example.modules.certificates.dtos.CertificateIssueDTO;
import com.example.modules.certificates.dtos.CertificateResponseDTO;
import com.example.modules.certificates.dtos.CertificatesSearchDTO;
import com.example.modules.certificates.services.CertificatesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = CERTIFICATES_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "certificates", description = "Operations related to certificates")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CertificatesController {

  CertificatesService certificatesService;

  @AllowRoles({ Role.ADMIN, Role.STUDENT, Role.INSTRUCTOR })
  @Operation(
    summary = "Retrieve all existing certificates (for ADMIN only)",
    responses = {
      @ApiResponse(responseCode = "200", description = "Certificates retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "403", description = "User is not an ADMIN", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping
  public PaginatedSuccessResponseDTO<CertificateResponseDTO> getAllCertificates(
    @ParameterObject @Valid CertificatesSearchDTO certificatesSearchDTO
  ) {
    return PaginatedSuccessResponseDTO.<CertificateResponseDTO>builder()
      .message("Lấy danh sách chứng chỉ thành công")
      .page(certificatesService.findAllCertificates(certificatesSearchDTO))
      .filters(certificatesSearchDTO.getFilters())
      .build();
  }

  @AllowRoles({ Role.ADMIN, Role.STUDENT, Role.INSTRUCTOR })
  @Operation(
    summary = "Issue a certificate (for ADMIN only)",
    description = "This endpoint should be used in case the scheduled task does not issue a certificate of a course to a user or other exceptions may happen.\n\n" +
      "This endpoint **DOES NOT** check whether the student has finished the course",
    responses = {
      @ApiResponse(responseCode = "200", description = "Certificate issued successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "403", description = "User is not an ADMIN", content = @Content),
      @ApiResponse(
        responseCode = "404",
        description = "Student or Course not found",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "409",
        description = "Certificate has already been issued for this student and course",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping
  public SuccessResponseDTO<CertificateResponseDTO> issueCertificate(
    @RequestBody @Valid CertificateIssueDTO certificateIssueDTO
  ) {
    return SuccessResponseDTO.<CertificateResponseDTO>builder()
      .message("Cấp chứng chỉ thành công")
      .data(certificatesService.issueCertificate(certificateIssueDTO))
      .build();
  }

  @AllowRoles(Role.ADMIN)
  @Operation(
    summary = "Revoke a certificate (for ADMIN only)",
    responses = {
      @ApiResponse(
        responseCode = "204",
        description = "Certificate revoked successfully",
        content = @Content
      ),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "403", description = "User is not an ADMIN", content = @Content),
      @ApiResponse(responseCode = "404", description = "Certificate not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revokeCertificate(@PathVariable String id) {
    certificatesService.revokeCertificate(id);
  }
}
