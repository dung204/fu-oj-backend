package com.example.modules.scores.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.modules.users.dtos.UserProfileDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class ScoreResponseDTO extends EntityDTO {

  @Schema(description = "User information")
  private UserProfileDTO user;

  @Schema(description = "Total score", example = "250.5")
  private Double totalScore;

  @Schema(description = "Number of easy problems solved", example = "10")
  private Integer solvedEasy;

  @Schema(description = "Number of medium problems solved", example = "5")
  private Integer solvedMedium;

  @Schema(description = "Number of hard problems solved", example = "2")
  private Integer solvedHard;

  @Schema(description = "Total number of problems solved", example = "17")
  private Integer totalSolved;
}
