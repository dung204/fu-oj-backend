package com.example.modules.certificates.dtos;

import com.example.base.dtos.PaginatedQueryDTO;
import com.example.base.utils.SwaggerExamples;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserCertificatesSearchDTO extends PaginatedQueryDTO {

  @Parameter(
    description = "Filter certificates by one or more course IDs.",
    array = @ArraySchema(schema = @Schema(example = SwaggerExamples.UUID))
  )
  private Set<String> courseId;
}
