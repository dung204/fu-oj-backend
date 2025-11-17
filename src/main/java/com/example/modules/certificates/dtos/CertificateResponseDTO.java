package com.example.modules.certificates.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.modules.courses.dtos.CourseResponseDTO;
import com.example.modules.users.dtos.UserProfileDTO;
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
public class CertificateResponseDTO extends EntityDTO {

  private UserProfileDTO user;
  private CourseResponseDTO course;
  private String reason;
}
