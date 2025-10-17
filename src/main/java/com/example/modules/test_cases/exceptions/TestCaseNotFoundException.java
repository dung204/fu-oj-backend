package com.example.modules.test_cases.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class TestCaseNotFoundException extends ResponseStatusException {

  public TestCaseNotFoundException() {
    super(HttpStatus.NOT_FOUND, "Test case not found");
  }
}
