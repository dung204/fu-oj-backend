package com.example.modules.groups.exeptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class GroupNotFoundException extends ResponseStatusException {

  public GroupNotFoundException() {
    super(HttpStatus.NOT_FOUND, "Group not found.");
  }
}
