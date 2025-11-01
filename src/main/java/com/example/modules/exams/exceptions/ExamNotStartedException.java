package com.example.modules.exams.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ExamNotStartedException extends ResponseStatusException {

  public ExamNotStartedException() {
    super(HttpStatus.BAD_REQUEST, "Exam has not started yet");
  }

  public ExamNotStartedException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }
}
