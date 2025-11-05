package com.example.modules.submissions.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class SubmissionNotFound extends ResponseStatusException {

  public SubmissionNotFound(String message) {
    super(HttpStatus.NOT_FOUND, message);
  }
}
