package com.example.modules.submissions.enums;

import com.example.modules.Judge0.dtos.Judge0SubmissionResponseDTO;
import java.util.List;
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
      Verdict.INTERNAL_ERROR,
      Verdict.EXEC_FORMAT_ERROR
    )
      .stream()
      .map(Verdict::getValue)
      .toList();
  }

  public static Verdict getVerdictFromJudge0Response(Judge0SubmissionResponseDTO response) {
    if (response.getExitCode() == 137) {
      return Verdict.MEMORY_LIMIT_EXCEEDED;
    }

    switch (response.getStatus().getId()) {
      case 1:
        return Verdict.IN_QUEUE;
      case 2:
        return Verdict.PROCESSING;
      case 3:
        return Verdict.ACCEPTED;
      case 4:
        return Verdict.WRONG_ANSWER;
      case 5:
        return Verdict.TIME_LIMIT_EXCEEDED;
      case 6:
        return Verdict.COMPILATION_ERROR;
      case 7:
      case 8:
      case 9:
      case 10:
      case 11:
      case 12:
        return Verdict.RUNTIME_ERROR;
      case 13:
        return Verdict.INTERNAL_ERROR;
      case 14:
        return Verdict.EXEC_FORMAT_ERROR;
      default:
        return Verdict.UNKNOWN;
    }
  }
}
