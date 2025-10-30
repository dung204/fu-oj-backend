package com.example.modules.exams.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Exam not found")
public class ExamNotFoundException extends RuntimeException {

  public ExamNotFoundException() {
    super("Exam not found");
  }

  public ExamNotFoundException(String message) {
    super(message);
  }
}
