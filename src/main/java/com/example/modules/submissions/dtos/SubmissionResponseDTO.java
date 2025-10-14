package com.example.modules.submissions.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.users.entities.User;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class SubmissionResponseDTO extends EntityDTO {

  private User user;
  private Exercise exercise;
  private String code;
  private String sourceCode;
  private String languageCode;
  private String time;
  private String memory;
  private String exerciseItem;
  private Integer passedTestCases;
  private Integer totalTestCases;

  public static SubmissionResponseDTO fromEntity(Submission submission) {
    return SubmissionResponseDTO.builder()
      .id(submission.getId())
      .user(submission.getUser())
      .exercise(submission.getExercise())
      .code(submission.getCode())
      .sourceCode(submission.getSourceCode())
      .languageCode(submission.getLanguageCode())
      .time(submission.getTime())
      .memory(submission.getMemory())
      .exerciseItem(submission.getExerciseItem())
      .passedTestCases(submission.getPassedTestCases())
      .totalTestCases(submission.getTotalTestCases())
      .build();
  }
}
