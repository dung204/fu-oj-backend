package com.example.modules.exams.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidTimeRangeException extends ResponseStatusException {

  public InvalidTimeRangeException() {
    super(HttpStatus.BAD_REQUEST, "Start time must be before end time");
  }
}
