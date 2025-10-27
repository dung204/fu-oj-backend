package com.example.modules.groups.exeptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class GroupHasAlreadyBeenUsedException extends ResponseStatusException {

  public GroupHasAlreadyBeenUsedException() {
    super(HttpStatus.CONFLICT, "Group has already been created.");
  }
}
