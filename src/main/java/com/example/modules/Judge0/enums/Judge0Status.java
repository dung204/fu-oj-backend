package com.example.modules.Judge0.enums;

import lombok.Getter;

@Getter
public enum Judge0Status {
  IN_QUEUE(1, "In Queue"),
  PROCESSING(2, "Processing"),
  ACCEPTED(3, "Accepted"),
  WRONG_ANSWER(4, "Wrong Answer"),
  TIME_LIMIT_EXCEEDED(5, "Time Limit Exceeded"),
  COMPILATION_ERROR(6, "Compilation Error"),
  RUNTIME_ERROR(7, "Runtime Error"),
  INTERNAL_ERROR(13, "INTERNAL_ERROR"),
  EXEC_FORMAT_ERROR(14, "EXEC_FORMAT_ERROR");

  private final int id;
  private final String description;

  Judge0Status(int id, String description) {
    this.id = id;
    this.description = description;
  }

  public static Judge0Status fromId(int id) {
    for (Judge0Status status : values()) {
      if (status.getId() == id) {
        return status;
      }
    }
    //
    throw new IllegalArgumentException("Unknown Judge0 status id: " + id);
  }
}
