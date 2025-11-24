package com.example.modules.courses.dtos;

import com.example.base.dtos.EntityDTO;
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
public class CourseResponseDTO extends EntityDTO {

  @Schema(description = "The title of the course.")
  private String title;

  @Schema(description = "A detailed description of the course content and objectives.")
  private String description;

  @Schema(description = "The image of the course", implementation = MinioFileResponse.class)
  private MinioFileResponse image;
}
