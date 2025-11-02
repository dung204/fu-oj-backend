package com.example.modules.exams.exceptions;

public class DuplicateExamSubmissionException extends RuntimeException {

  public DuplicateExamSubmissionException(String message) {
    super(message);
  }
}
