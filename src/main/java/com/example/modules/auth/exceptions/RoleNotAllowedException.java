package com.example.modules.auth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RoleNotAllowedException extends ResponseStatusException {

  public RoleNotAllowedException() {
    super(HttpStatus.FORBIDDEN, "Vai trò hiện tại không được phép thực hiện thao tác này");
  }
}
