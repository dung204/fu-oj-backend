package com.example.modules.exams.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.modules.users.dtos.UserProfileDtoV2;
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
public class ExamRankingResponseDto extends EntityDTO {

  private UserProfileDtoV2 user;

  private ExamResultResponseDto exam;

  private Double totalScore;
}
