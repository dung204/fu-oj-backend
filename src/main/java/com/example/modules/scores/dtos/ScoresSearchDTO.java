package com.example.modules.scores.dtos;

import com.example.base.annotations.OrderParam;
import com.example.base.dtos.PaginatedQueryDTO;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScoresSearchDTO extends PaginatedQueryDTO {

  @Parameter(description = "Filter by user ID")
  private String userId;

  @Parameter(description = "Filter by user email")
  private String userEmail;

  @Parameter(description = "Minimum total score")
  private Double minScore;

  @Parameter(description = "Maximum total score")
  private Double maxScore;

  @Parameter(
    description = "Sorting order. Format: `{field}:{order}`. " +
      "Allowed fields: `totalScore`, `solvedHard`, `solvedMedium`, `solvedEasy`, " +
      "`createdTimestamp`, `updatedTimestamp`, `deletedTimestamp`. " +
      "Default: Sort by ranking (totalScore desc, solvedHard desc, solvedMedium desc, solvedEasy desc)"
  )
  public List<
    @OrderParam(
      allowedFields = {
        "totalScore",
        "solvedHard",
        "solvedMedium",
        "solvedEasy",
        "createdTimestamp",
        "updatedTimestamp",
        "deletedTimestamp",
      }
    ) String
  > order = List.of("totalScore:desc", "solvedHard:desc", "solvedMedium:desc", "solvedEasy:desc");
}
