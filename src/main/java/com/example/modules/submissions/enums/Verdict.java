package com.example.modules.submissions.enums;

import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public enum Verdict {
  IN_QUEUE("IN_QUEUE"),
  PROCESSING("PROCESSING"),
  ACCEPTED("ACCEPTED"),
  WRONG_ANSWER("WRONG_ANSWER"),
  TIME_LIMIT_EXCEEDED("TIME_LIMIT_EXCEEDED"),
  MEMORY_LIMIT_EXCEEDED("MEMORY_LIMIT_EXCEEDED"),
  COMPILATION_ERROR("COMPILATION_ERROR"),
  RUNTIME_ERROR("RUNTIME_ERROR"),
  RETURN_ERROR("RETURN_ERROR"),
  INVALID_RETURN("INVALID_RETURN"),
  INTERNAL_ERROR("INTERNAL_ERROR"),
  EXEC_FORMAT_ERROR("EXEC_FORMAT_ERROR"),
  UNKNOWN("UNKNOWN");

  String value;

  public static List<String> getProcessingVerdicts() {
    return List.of(Verdict.IN_QUEUE, Verdict.PROCESSING).stream().map(Verdict::getValue).toList();
  }

  public static List<String> getErrorVerdicts() {
    return List.of(
      Verdict.WRONG_ANSWER,
      Verdict.TIME_LIMIT_EXCEEDED,
      Verdict.MEMORY_LIMIT_EXCEEDED,
      Verdict.COMPILATION_ERROR,
      Verdict.RUNTIME_ERROR,
      Verdict.RETURN_ERROR,
      Verdict.INVALID_RETURN,
      Verdict.INTERNAL_ERROR,
      Verdict.EXEC_FORMAT_ERROR
    )
      .stream()
      .map(Verdict::getValue)
      .toList();
  }
}
