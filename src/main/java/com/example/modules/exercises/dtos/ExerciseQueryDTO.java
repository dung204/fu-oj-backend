package com.example.modules.exercises.dtos;

import com.example.base.annotations.OrderParam;
import com.example.base.dtos.PaginatedQueryDTO;
import com.example.modules.exercises.enums.Difficulty;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExerciseQueryDTO extends PaginatedQueryDTO {

  @Parameter(
    description = "Filter by exercise code (partial match) or exercise title (partial match)"
  )
  private String query;

  @Parameter(
    description = "Filter by exercise code (partial match) or exercise title (partial match)",
    schema = @Schema(implementation = Difficulty.class)
  )
  private String difficulty;

  @Parameter(
    description = "Filter by exercise code (partial match) or exercise title (partial match)",
    schema = @Schema(implementation = Difficulty.class)
  )
  private String visibility;

  @Parameter(description = "Filter by topic ID")
  private List<String> topic;

  @Parameter(
    description = "Sorting fields. Allowed: code, title, maxSubmissions, createdTimestamp, updatedTimestamp"
  )
  public List<
    @OrderParam(
      allowedFields = {
        "code",
        "title",
        "difficulty",
        "visibility",
        "maxSubmissions",
        "createdTimestamp",
        "updatedTimestamp",
        "deletedTimestamp",
      }
    ) String
  > order = Collections.emptyList();
}
