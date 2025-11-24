package com.example.modules.submissions.utils;

import com.example.modules.submission_results.dtos.SubmissionResultResponseDTO;
import com.example.modules.submission_results.entities.SubmissionResult;
import com.example.modules.test_cases.utils.TestCaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { TestCaseMapper.class })
@Slf4j
public abstract class SubmissionResultMapper {

  @Mapping(source = "testCase", target = "testCase")
  public abstract SubmissionResultResponseDTO toSubmissionResultResponseDTO(
    SubmissionResult submissionResult
  );
}
