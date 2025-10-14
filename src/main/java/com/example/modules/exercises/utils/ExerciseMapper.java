package com.example.modules.exercises.utils;

import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import com.example.modules.exercises.entities.Exercise;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class ExerciseMapper {

  @Mapping(
    target = "testCasesCount",
    expression = "java(exercise.getTestCases() != null ? exercise.getTestCases().size() : 0)"
  )
  public abstract ExerciseResponseDTO toExerciseResponseDTO(Exercise exercise);
}
