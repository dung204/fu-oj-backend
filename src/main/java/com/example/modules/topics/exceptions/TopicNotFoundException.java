package com.example.modules.topics.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class TopicNotFoundException extends ResponseStatusException {

  public TopicNotFoundException() {
    super(HttpStatus.NOT_FOUND, "Topic not found.");
  }
}
