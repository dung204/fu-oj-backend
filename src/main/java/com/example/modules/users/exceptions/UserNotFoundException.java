package com.example.modules.users.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserNotFoundException extends ResponseStatusException {

  public UserNotFoundException() {
    super(HttpStatus.NOT_FOUND, "User not found");
  }
}
