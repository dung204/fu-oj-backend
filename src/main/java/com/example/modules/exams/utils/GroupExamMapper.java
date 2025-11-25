package com.example.modules.exams.utils;

import com.example.modules.exams.dtos.GroupExamResponseDTO;
import com.example.modules.exams.entities.Exam;
import com.example.modules.exams.entities.GroupExam;
import com.example.modules.groups.entities.Group;
import com.example.modules.users.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public abstract class GroupExamMapper {

  @Mapping(target = "groupExamId", source = "id")
  @Mapping(target = "exam", expression = "java(mapExam(groupExam.getExam()))")
  @Mapping(target = "group", expression = "java(mapGroup(groupExam.getGroup()))")
  @Mapping(
    target = "status",
    expression = "java(groupExam.getStatus() != null ? groupExam.getStatus().name() : null)"
  )
  public abstract GroupExamResponseDTO toGroupExamResponseDTO(GroupExam groupExam);

  @Named("mapExam")
  protected GroupExamResponseDTO.ExamBasicInfoDTO mapExam(Exam exam) {
    if (exam == null) return null;

    return GroupExamResponseDTO.ExamBasicInfoDTO.builder()
      .id(exam.getId())
      .code(exam.getCode())
      .title(exam.getTitle())
      .description(exam.getDescription())
      .startTime(exam.getStartTime() != null ? exam.getStartTime().toString() : null)
      .endTime(exam.getEndTime() != null ? exam.getEndTime().toString() : null)
      .timeLimit(exam.getTimeLimit())
      .build();
  }

  @Named("mapGroup")
  protected GroupExamResponseDTO.GroupBasicInfoDTO mapGroup(Group group) {
    if (group == null) return null;

    User instructor = group.getInstructor();
    String ownerName = null;
    if (instructor != null) {
      String first = instructor.getFirstName() != null ? instructor.getFirstName() : "";
      String last = instructor.getLastName() != null ? instructor.getLastName() : "";
      ownerName = (first + " " + last).trim();
      if (ownerName.isEmpty()) ownerName = null;
    }

    return GroupExamResponseDTO.GroupBasicInfoDTO.builder()
      .id(group.getId())
      .code(group.getCode())
      .name(group.getName())
      .ownerId(instructor != null ? instructor.getId() : null)
      .ownerName(ownerName)
      .build();
  }
}
