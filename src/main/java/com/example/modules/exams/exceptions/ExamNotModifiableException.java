package com.example.modules.exams.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ExamNotModifiableException extends ResponseStatusException {

  public ExamNotModifiableException() {
    super(HttpStatus.CONFLICT, "Không thể cập nhật kỳ thi này.");
  }

  public ExamNotModifiableException(String message) {
    super(HttpStatus.CONFLICT, message);
  }
}
