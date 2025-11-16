package com.example.modules.exams.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.modules.users.dtos.UserProfileWithoutAvatarDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExamRankingResponseDTO extends EntityDTO {

  private UserProfileWithoutAvatarDTO user;

  private ExamResultResponseDTO exam;

  private Double totalScore;

  private boolean completed;
}
