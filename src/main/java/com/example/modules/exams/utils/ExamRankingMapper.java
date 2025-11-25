package com.example.modules.exams.utils;

import com.example.modules.exams.dtos.ExamRankingResponseDTO;
import com.example.modules.exams.dtos.GroupExamResultResponseDTO;
import com.example.modules.exams.entities.ExamRanking;
import com.example.modules.exams.entities.GroupExam;
import com.example.modules.users.entities.User;
import com.example.modules.users.utils.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public abstract class ExamRankingMapper {

  @Mapping(target = "user", qualifiedByName = "toUserProfileWithoutAvatarDTO")
  @Mapping(
    target = "groupExam",
    expression = "java(mapGroupExam(ranking.getGroupExam(), ranking.getUser()))"
  )
  public abstract ExamRankingResponseDTO toExamRankingResponseDto(ExamRanking ranking);

  @Named("mapGroupExam")
  protected GroupExamResultResponseDTO mapGroupExam(GroupExam groupExam, User user) {
    if (groupExam == null) return null;

    var exam = groupExam.getExam();
    var group = groupExam.getGroup();

    return GroupExamResultResponseDTO.builder()
      .groupExamId(groupExam.getId())
      .examId(exam != null ? exam.getId() : null)
      .examCode(exam != null ? exam.getCode() : null)
      .examTitle(exam != null ? exam.getTitle() : null)
      .groupId(group != null ? group.getId() : null)
      .groupName(group != null ? group.getName() : null)
      .startTime(exam != null ? exam.getStartTime() : null)
      .endTime(exam != null ? exam.getEndTime() : null)
      .timeLimit(exam != null ? exam.getTimeLimit() : null)
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
