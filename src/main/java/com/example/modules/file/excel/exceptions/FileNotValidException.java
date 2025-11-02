package com.example.modules.file.excel.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class FileNotValidException extends ResponseStatusException {

  public FileNotValidException() {
    super(HttpStatus.CONFLICT, "File is not valid");
  }
}
