package com.example.modules.submissions.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SubmissionNotFoundException extends ResponseStatusException {

  public SubmissionNotFoundException(String message) {
    super(HttpStatus.NOT_FOUND, message);
  }
}
