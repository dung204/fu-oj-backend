package com.example.modules.courses.dtos;

import com.example.base.utils.SwaggerExamples;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseExerciseRequestDTO {

  @ArraySchema(schema = @Schema(example = SwaggerExamples.UUID))
  @NotEmpty
  private Set<String> exerciseIds;
}
