package com.example.modules.auth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PasswordNotMatchException extends ResponseStatusException {

  public PasswordNotMatchException() {
    super(HttpStatus.BAD_REQUEST, "Password not match.");
  }
}
