package com.example.modules.exams.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Không tìm thấy kỳ thi")
public class ExamNotFoundException extends RuntimeException {

  public ExamNotFoundException() {
    super("Không tìm thấy kỳ thi");
  }

  public ExamNotFoundException(String message) {
    super(message);
  }
}
