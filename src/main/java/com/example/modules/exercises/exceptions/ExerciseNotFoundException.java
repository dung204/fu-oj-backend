package com.example.modules.exercises.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ExerciseNotFoundException extends ResponseStatusException {

  public ExerciseNotFoundException() {
    super(HttpStatus.NOT_FOUND, "Exercise not found");
  }

  public ExerciseNotFoundException(String message) {
    super(HttpStatus.NOT_FOUND, message);
  }
}
