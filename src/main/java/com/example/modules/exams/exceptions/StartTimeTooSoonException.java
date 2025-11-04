package com.example.modules.exams.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class StartTimeTooSoonException extends ResponseStatusException {

  public StartTimeTooSoonException() {
    super(HttpStatus.BAD_REQUEST, "Start time is too soon");
  }

  public StartTimeTooSoonException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }
}
