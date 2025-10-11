package com.example.modules.exercises.dtos;

import com.example.base.annotations.OrderParam;
import com.example.base.dtos.PaginatedQueryDTO;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExerciseQueryDTO extends PaginatedQueryDTO {

  @Parameter(description = "Filter by exercise code")
  private String code;

  @Parameter(description = "Filter by exercise title (partial match)")
  private String title;

  @Parameter(description = "Filter by topic ID")
  private String topicId;

  @Parameter(description = "Filter by group ID - get exercises of a specific group")
  private String groupId;

  @Parameter(
    description = "Sorting fields. Allowed: code, title, maxSubmissions, createdTimestamp, updatedTimestamp"
  )
  public List<
    @OrderParam(
      allowedFields = {
        "code",
        "title",
        "maxSubmissions",
        "createdTimestamp",
        "updatedTimestamp",
        "deletedTimestamp",
      }
    ) String
  > order = Collections.emptyList();
}
