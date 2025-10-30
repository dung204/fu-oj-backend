package com.example.modules.exams.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
  UPCOMING("UPCOMING", "UPCOMING"),
  ONGOING("ONGOING", "ONGOING"),
  COMPLETED("COMPLETED", "COMPLETED"),
  DRAFT("DRAFT", "DRAFT"),
  CANCELED("CANCELED", "CANCELED"),
  OUTDATED("OUTDATED", "OUTDATED");

  private final String status;
  private final String value;
}
