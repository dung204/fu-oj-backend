package com.example.modules.auth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EmailHasAlreadyBeenUsedException extends ResponseStatusException {

  public EmailHasAlreadyBeenUsedException() {
    super(HttpStatus.CONFLICT, "Email has already been registered.");
  }
}
