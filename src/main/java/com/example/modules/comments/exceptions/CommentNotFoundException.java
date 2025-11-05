package com.example.modules.comments.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CommentNotFoundException extends ResponseStatusException {

  public CommentNotFoundException() {
    super(HttpStatus.NOT_FOUND, "Comment not found");
  }
}
