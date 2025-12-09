package com.example.modules.auth.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class TokenInvalidatedException extends ResponseStatusException {

  public TokenInvalidatedException() {
    super(HttpStatus.UNAUTHORIZED, "Token đã bị vô hiệu hóa");
  }
}
