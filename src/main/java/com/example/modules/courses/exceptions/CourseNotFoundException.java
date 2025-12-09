package com.example.modules.courses.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CourseNotFoundException extends ResponseStatusException {

  public CourseNotFoundException() {
    super(HttpStatus.NOT_FOUND, "Không tìm thấy khóa học");
  }
}
