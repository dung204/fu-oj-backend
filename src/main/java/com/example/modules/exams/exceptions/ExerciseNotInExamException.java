package com.example.modules.exams.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ExerciseNotInExamException extends ResponseStatusException {

  public ExerciseNotInExamException() {
    super(HttpStatus.BAD_REQUEST, "Bài tập không thuộc kỳ thi này");
  }

  public ExerciseNotInExamException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }
}
