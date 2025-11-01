package com.example.modules.scores.utils;

import com.example.modules.scores.dtos.ScoreResponseDTO;
import com.example.modules.scores.entities.Score;
import com.example.modules.users.utils.UserMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public abstract class ScoresMapper {

  @Mapping(source = "user", target = "user", qualifiedByName = "toUserProfileDTO")
  @Mapping(source = "totalScore", target = "totalScore")
  @Mapping(source = "solvedEasy", target = "solvedEasy")
  @Mapping(source = "solvedMedium", target = "solvedMedium")
  @Mapping(source = "solvedHard", target = "solvedHard")
  @Mapping(target = "totalSolved", ignore = true)
  public abstract ScoreResponseDTO toScoreResponseDTO(Score score);

  @AfterMapping
  protected void calculateTotalSolved(Score score, @MappingTarget ScoreResponseDTO dto) {
    int totalSolved =
      (score.getSolvedEasy() != null ? score.getSolvedEasy() : 0) +
      (score.getSolvedMedium() != null ? score.getSolvedMedium() : 0) +
      (score.getSolvedHard() != null ? score.getSolvedHard() : 0);
    dto.setTotalSolved(totalSolved);
  }
}
