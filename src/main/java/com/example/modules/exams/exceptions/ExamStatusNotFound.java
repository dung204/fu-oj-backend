package com.example.modules.exams.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ExamStatusNotFound extends ResponseStatusException {

  public ExamStatusNotFound() {
    super(HttpStatus.NOT_FOUND, "Exam status not found");
  }
}
