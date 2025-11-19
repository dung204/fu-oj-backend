package com.example.modules.certificates.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.modules.courses.dtos.CourseResponseDTO;
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
public class UserCertificateResponseDTO extends EntityDTO {

  private CourseResponseDTO course;
  private String reason;
}
