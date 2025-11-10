package com.example.modules.courses.dtos;

import com.example.base.utils.SwaggerExamples;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseExerciseRequestDTO {

  @ArraySchema(schema = @Schema(example = SwaggerExamples.UUID))
  private Set<String> exerciseIds = Collections.emptySet();
}
