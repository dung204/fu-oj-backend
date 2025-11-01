package com.example.modules.exams.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class StudentNotInGroupException extends ResponseStatusException {

  public StudentNotInGroupException() {
    super(HttpStatus.FORBIDDEN, "Student is not in the exam's group");
  }

  public StudentNotInGroupException(String message) {
    super(HttpStatus.FORBIDDEN, message);
  }
}
