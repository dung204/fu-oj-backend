package com.example.modules.Judge0.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class Judge0Exception extends ResponseStatusException {

  public Judge0Exception(HttpStatus status, String reason) {
    super(status, reason);
  }

  public Judge0Exception(int status, String reason) {
    super(HttpStatus.valueOf(status), reason);
  }
}
