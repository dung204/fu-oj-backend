package com.example.modules.exams.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ExamNotStartedException extends ResponseStatusException {

  public ExamNotStartedException() {
    super(HttpStatus.BAD_REQUEST, "Kỳ thi chưa bắt đầu");
  }

  public ExamNotStartedException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }
}
