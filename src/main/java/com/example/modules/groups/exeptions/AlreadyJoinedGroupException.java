package com.example.modules.groups.exeptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AlreadyJoinedGroupException extends ResponseStatusException {

  public AlreadyJoinedGroupException() {
    super(HttpStatus.CONFLICT, "Already joined this group.");
  }
}
