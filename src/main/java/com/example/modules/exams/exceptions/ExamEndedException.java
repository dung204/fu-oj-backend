package com.example.modules.exams.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ExamEndedException extends ResponseStatusException {

  public ExamEndedException() {
    super(HttpStatus.BAD_REQUEST, "Exam has already ended");
  }

  public ExamEndedException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }
}
