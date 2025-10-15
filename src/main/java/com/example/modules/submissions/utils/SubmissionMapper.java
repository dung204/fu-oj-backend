package com.example.modules.submissions.utils;

import com.example.modules.exercises.utils.ExerciseMapper;
import com.example.modules.submissions.dtos.SubmissionResponseDTO;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.users.utils.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { UserMapper.class, ExerciseMapper.class })
public abstract class SubmissionMapper {

  @Mapping(source = "user", target = "user", qualifiedByName = "toUserProfileDTO")
  @Mapping(source = "exercise", target = "exercise", qualifiedByName = "toExerciseResponseDTO")
  public abstract SubmissionResponseDTO toSubmissionResponseDTO(Submission submission);
}
