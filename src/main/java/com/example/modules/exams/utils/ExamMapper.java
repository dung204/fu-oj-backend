package com.example.modules.exams.utils;

import com.example.modules.exams.dtos.ExamResponseDTO;
import com.example.modules.exams.entities.Exam;
import com.example.modules.exams.entities.ExamExercise;
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
  @Mapping(
    source = "examExercises",
    target = "exercises",
    qualifiedByName = "mapExamExercisesToExercises"
  )
  public abstract ExamResponseDTO toExamResponseDTO(Exam exam);

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
