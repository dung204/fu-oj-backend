package com.example.modules.auth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidCredentialsException extends ResponseStatusException {

  public InvalidCredentialsException() {
    super(HttpStatus.UNAUTHORIZED, "Email or password is incorrect");
  }

  public InvalidCredentialsException(String message) {
    super(HttpStatus.UNAUTHORIZED, message);
  }
}
