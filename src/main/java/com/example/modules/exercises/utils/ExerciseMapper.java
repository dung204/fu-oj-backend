package com.example.modules.exercises.utils;

import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.test_cases.dtos.TestCaseResponseDTO;
import com.example.modules.test_cases.entities.TestCase;
import com.example.modules.test_cases.utils.TestCaseMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class ExerciseMapper {

  @Autowired
  protected TestCaseMapper testCaseMapper;

  @Named("toExerciseResponseDTO")
  @Mapping(
    target = "testCasesCount",
    expression = "java(exercise.getTestCases() != null ? exercise.getTestCases().size() : 0)"
  )
  @Mapping(source = "testCases", target = "testCases", qualifiedByName = "hidePrivateTestCases")
  public abstract ExerciseResponseDTO toExerciseResponseDTOWithPrivateTestCasesHidden(
    Exercise exercise
  );

  @Named("toExerciseResponseDTOWithAllTestCases")
  @Mapping(
    target = "testCasesCount",
    expression = "java(exercise.getTestCases() != null ? exercise.getTestCases().size() : 0)"
  )
  @Mapping(source = "testCases", target = "testCases", qualifiedByName = "mapAllTestCases")
  public abstract ExerciseResponseDTO toExerciseResponseDTOWithAllTestCases(Exercise exercise);

  @Named("hidePrivateTestCases")
  protected List<TestCaseResponseDTO> hidePrivateTestCases(List<TestCase> testCases) {
    return testCases
      .stream()
      .map(tc -> {
        if (!tc.getIsPublic()) {
          tc.setInput(null);
          tc.setOutput(null);
          tc.setNote(null);
        }

        return testCaseMapper.toTestCaseResponseDTO(tc);
      })
      .toList();
  }

  @Named("mapAllTestCases")
  protected List<TestCaseResponseDTO> mapAllTestCases(List<TestCase> testCases) {
    return testCases.stream().map(testCaseMapper::toTestCaseResponseDTO).toList();
  }
}
