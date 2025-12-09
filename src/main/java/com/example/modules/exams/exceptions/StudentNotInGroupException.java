package com.example.modules.exams.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class StudentNotInGroupException extends ResponseStatusException {

  public StudentNotInGroupException() {
    super(HttpStatus.FORBIDDEN, "Sinh viên không thuộc nhóm của kỳ thi");
  }

  public StudentNotInGroupException(String message) {
    super(HttpStatus.FORBIDDEN, message);
  }
}
