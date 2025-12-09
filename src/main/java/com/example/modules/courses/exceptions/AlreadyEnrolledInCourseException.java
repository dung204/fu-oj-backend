package com.example.modules.courses.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AlreadyEnrolledInCourseException extends ResponseStatusException {

  public AlreadyEnrolledInCourseException() {
    super(HttpStatus.CONFLICT, "Bạn đã đăng ký khóa học này rồi.");
  }
}
