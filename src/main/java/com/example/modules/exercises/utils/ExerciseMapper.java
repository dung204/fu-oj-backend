package com.example.modules.exercises.utils;

import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import com.example.modules.exercises.entities.Exercise;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class ExerciseMapper {

  public abstract ExerciseResponseDTO toExerciseResponseDTO(Exercise exercise);
}
