package com.example.modules.exams.utils;

import com.example.modules.exams.dtos.ExamRankingResponseDTO;
import com.example.modules.exams.dtos.ExamResultResponseDto;
import com.example.modules.exams.entities.Exam;
import com.example.modules.exams.entities.ExamRanking;
import com.example.modules.users.entities.User;
import com.example.modules.users.utils.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public abstract class ExamRankingMapper {

  @Mapping(target = "user", qualifiedByName = "toUserProfileWithoutAvatarDTO")
  @Mapping(target = "exam", expression = "java(mapExam(ranking.getExam(), ranking.getUser()))")
  public abstract ExamRankingResponseDTO toExamRankingResponseDto(ExamRanking ranking);

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
