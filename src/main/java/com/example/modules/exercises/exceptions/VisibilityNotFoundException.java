package com.example.modules.exercises.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class VisibilityNotFoundException extends ResponseStatusException {

  public VisibilityNotFoundException() {
    super(HttpStatus.NOT_FOUND, "Visibility enums not found");
  }
}
