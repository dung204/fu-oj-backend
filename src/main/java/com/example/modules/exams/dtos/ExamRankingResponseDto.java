package com.example.modules.exams.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.modules.exams.entities.Exam;
import com.example.modules.users.entities.User;
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

  private User user;

  private Exam exam;

  private Double totalScore;
}
