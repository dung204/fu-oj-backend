package com.example.modules.submissions.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import com.example.modules.users.dtos.UserProfileDTO;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class SubmissionResponseDTO extends EntityDTO {

  private UserProfileDTO user;
  private ExerciseResponseDTO exercise;
  private String code;
  private String sourceCode;
  private String languageCode;
  private String time;
  private String memory;
  private String exerciseItem;
  private Integer passedTestCases;
  private Integer totalTestCases;
}
