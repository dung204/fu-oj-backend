package com.example.modules.exams.utils;

import com.example.modules.exams.dtos.ExamResponseDTO;
import com.example.modules.exams.entities.Exam;
import com.example.modules.exams.entities.ExamExercise;
import com.example.modules.exams.entities.GroupExam;
import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import com.example.modules.exercises.utils.ExerciseMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class ExamMapper {

  @Autowired
  protected ExerciseMapper exerciseMapper;

  @Named("toExamResponseDTO")
  @Mapping(source = "groupExams", target = "groups", qualifiedByName = "mapGroupExamsToGroupInfos")
  @Mapping(
    source = "examExercises",
    target = "exercises",
    qualifiedByName = "mapExamExercisesToExercises"
  )
  public abstract ExamResponseDTO toExamResponseDTO(Exam exam);

  @Named("mapGroupExamsToGroupInfos")
  protected List<ExamResponseDTO.GroupInfo> mapGroupExamsToGroupInfos(List<GroupExam> groupExams) {
    if (groupExams == null || groupExams.isEmpty()) {
      return List.of();
    }

    return groupExams
      .stream()
      .map(ge ->
        ExamResponseDTO.GroupInfo.builder()
          .id(ge.getGroup().getId())
          .code(ge.getGroup().getCode())
          .name(ge.getGroup().getName())
          .description(ge.getGroup().getDescription())
          .isPublic(ge.getGroup().getIsPublic())
          .build()
      )
      .collect(Collectors.toList());
  }

  @Named("mapExamExercisesToExercises")
  protected List<ExerciseResponseDTO> mapExamExercisesToExercises(
    List<ExamExercise> examExercises
  ) {
    if (examExercises == null || examExercises.isEmpty()) {
      return List.of();
    }

    return examExercises
      .stream()
      .map(ee -> exerciseMapper.toExerciseResponseDTOWithAllTestCases(ee.getExercise()))
      .collect(Collectors.toList());
  }
}
