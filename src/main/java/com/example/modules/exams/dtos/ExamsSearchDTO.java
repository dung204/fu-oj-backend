package com.example.modules.exams.dtos;

import com.example.base.dtos.PaginatedQueryDTO;
import com.example.modules.exams.enums.ExamStatus;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExamsSearchDTO extends PaginatedQueryDTO {

  @Parameter(description = "Filter by exam code (partial match) or exam title (partial match)")
  private String query;

  @Parameter(
    description = "Filter by exam status. Can be specified multiple times (e.g., ?status=UPCOMING&status=ONGOING).",
    array = @ArraySchema(schema = @Schema(implementation = ExamStatus.class))
  )
  private Set<String> status;

  @Parameter(description = "Filter exams by a specific group ID.")
  private String groupId;
}
