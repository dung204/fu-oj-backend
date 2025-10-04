package com.example.modules.submissions.utils;

import com.example.modules.submissions.dtos.SubmissionResponseDTO;
import com.example.modules.submissions.entities.Submission;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class SubmissionMapper {

  public abstract SubmissionResponseDTO toSubmissionResponseDTO(Submission submission);
}
