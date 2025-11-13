package com.example.modules.file.excel.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AccountImportLimitExceeded extends ResponseStatusException {

  public AccountImportLimitExceeded() {
    super(HttpStatus.BAD_REQUEST, "Account Import Limit Exceeded");
  }
}
