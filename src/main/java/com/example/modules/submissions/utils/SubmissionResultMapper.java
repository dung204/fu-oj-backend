package com.example.modules.submissions.utils;

import com.example.modules.submission_results.dtos.SubmissionResultResponseDTO;
import com.example.modules.submission_results.entities.SubmissionResult;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class SubmissionResultMapper {

  public abstract SubmissionResultResponseDTO toSubmissionResponseDTO(
    SubmissionResult submissionResult
  );
}
