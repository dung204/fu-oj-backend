package com.example.modules.test_cases.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.test_cases.entities.TestCase;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestCasesSpecification extends SpecificationBuilder<TestCase> {

  public static TestCasesSpecification builder() {
    return new TestCasesSpecification();
  }
}
