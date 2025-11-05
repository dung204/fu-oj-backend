package com.example.modules.comments.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CommentOperationNotAllowedException extends ResponseStatusException {

  public CommentOperationNotAllowedException() {
    super(HttpStatus.FORBIDDEN, "Operations for this comment is not allowed!");
  }
}
