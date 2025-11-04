package com.example.modules.exams.enums;

import com.example.modules.exams.exceptions.ExamStatusNotFound;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExamStatus {
  UPCOMING("UPCOMING", "UPCOMING"),
  ONGOING("ONGOING", "ONGOING"),
  COMPLETED("COMPLETED", "COMPLETED"),
  DRAFT("DRAFT", "DRAFT"),
  CANCELED("CANCELED", "CANCELED"),
  OUTDATED("OUTDATED", "OUTDATED");

  private final String status;
  private final String value;

  public static ExamStatus fromValue(String value) {
    return Stream.of(ExamStatus.values())
      .filter(status -> status.getValue().equals(value))
      .findFirst()
      .orElseThrow(ExamStatusNotFound::new);
  }
}
