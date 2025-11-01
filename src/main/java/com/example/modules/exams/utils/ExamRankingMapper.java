package com.example.modules.exams.utils;

import com.example.modules.exams.dtos.ExamRankingResponseDto;
import com.example.modules.exams.dtos.ExamResultResponseDto;
import com.example.modules.exams.entities.Exam;
import com.example.modules.exams.entities.ExamRanking;
import com.example.modules.users.dtos.UserProfileDtoV2;
import com.example.modules.users.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public abstract class ExamRankingMapper {

  @Mapping(target = "user", expression = "java(mapUser(ranking.getUser()))")
  @Mapping(target = "exam", expression = "java(mapExam(ranking.getExam(), ranking.getUser()))")
  public abstract ExamRankingResponseDto toExamRankingResponseDto(ExamRanking ranking);

  @Named("mapUser")
  protected UserProfileDtoV2 mapUser(User user) {
    if (user == null) return null;
    return UserProfileDtoV2.builder()
      .id(user.getId())
      .email(user.getAccount() != null ? user.getAccount().getEmail() : null)
      .role(
        user.getAccount() != null && user.getAccount().getRole() != null
          ? user.getAccount().getRole().getValue()
          : null
      )
      .firstName(user.getFirstName())
      .lastName(user.getLastName())
      .rollNumber(user.getRollNumber())
      .build();
  }

  @Named("mapExam")
  protected ExamResultResponseDto mapExam(Exam exam, User user) {
    if (exam == null) return null;
    return ExamResultResponseDto.builder()
      .examId(exam.getId())
      .examCode(exam.getCode())
      .examTitle(exam.getTitle())
      .startTime(exam.getStartTime())
      .endTime(exam.getEndTime())
      .userId(user != null ? user.getId() : null)
      .userName(user != null ? buildUserName(user) : null)
      .build();
  }

  private String buildUserName(User user) {
    String first = user.getFirstName() != null ? user.getFirstName() : "";
    String last = user.getLastName() != null ? user.getLastName() : "";
    String full = (first + " " + last).trim();
    return full.isEmpty() ? null : full;
  }
}
