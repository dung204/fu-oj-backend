package com.example.modules.test_cases.utils;

import com.example.modules.test_cases.dtos.TestCaseResponseDTO;
import com.example.modules.test_cases.entities.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class TestCaseMapper {

  public abstract TestCaseResponseDTO toTestCaseResponseDTO(TestCase testCase);
}
