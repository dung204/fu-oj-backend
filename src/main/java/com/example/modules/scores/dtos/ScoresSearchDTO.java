package com.example.modules.scores.dtos;

import com.example.base.dtos.PaginatedQueryDTO;
import io.swagger.v3.oas.annotations.Parameter;
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
}
