package com.example.modules.exercises.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class DifficultyNotFoundException extends ResponseStatusException {

  public DifficultyNotFoundException() {
    super(HttpStatus.NOT_FOUND, "Difficulty enums not found");
  }
}
